/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.transform.ast

import groovy.transform.CompileStatic

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.tools.GenericsUtils
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.util.HashCodeHelper

import yakworks.commons.transform.IdEqualsHashCode

import static org.codehaus.groovy.ast.ClassHelper.make
import static org.codehaus.groovy.ast.tools.GeneralUtils.andX
import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS
import static org.codehaus.groovy.ast.tools.GeneralUtils.eqX
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceProperties
import static org.codehaus.groovy.ast.tools.GeneralUtils.hasDeclaredMethod
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS
import static org.codehaus.groovy.ast.tools.GeneralUtils.isInstanceOfX
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX
import static org.codehaus.groovy.ast.tools.GeneralUtils.param
import static org.codehaus.groovy.ast.tools.GeneralUtils.params
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS
import static org.codehaus.groovy.ast.tools.GeneralUtils.sameX
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX
import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafe

/**
 * The AST for identity objects. Used mostly for gorm and hibernate
 *
 * @author Joshua Burnett (@basejump)
 */
@SuppressWarnings
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
@CompileStatic
public class IdEqualsHashASTTransformation extends AbstractASTTransformation {
    static final Class MY_CLASS = IdEqualsHashCode
    static final ClassNode MY_TYPE = make(MY_CLASS)
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage()
    private static final ClassNode HASHUTIL_TYPE = make(HashCodeHelper)
    private static final ClassNode OBJECT_TYPE = makeClassSafe(Object)

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source)
        AnnotatedNode parent = (AnnotatedNode) nodes[1]
        AnnotationNode anno = (AnnotationNode) nodes[0]
        if (!MY_TYPE==anno.getClassNode()) return

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent
            if (!checkNotInterface(cNode, MY_TYPE_NAME)) return
            List<String> includes = getMemberList(anno, "includes");
            List<String> hashKey = getMemberList(anno, "hashKey");
            createHashCode(cNode, hashKey);
            createEqualsForId(cNode, includes);
        }
    }

    public static void createHashCode(ClassNode cNode, List<String> includes) {
        // make a public method if none exists otherwise try a private method with leading underscore
        boolean hasExistingHashCode = hasDeclaredMethod(cNode, "hashCode", 0);
        if (hasExistingHashCode && hasDeclaredMethod(cNode, "_hashCode", 0)) return;

        final BlockStatement methodBody = new BlockStatement();

        final BlockStatement body = new BlockStatement();
        // def _result = HashCodeHelper.initHash()
        final Expression result = varX("_result");
        body.addStatement(declS(result, callX(HASHUTIL_TYPE, "initHash")));


        final BlockStatement fieldsBody = new BlockStatement();
        for (String field : includes) {
            // _result = HashCodeHelper.updateHash(_result, getProperty()) // plus self-reference checking
            // Expression getter = getterThisX(cNode, pNode);
            Expression getter = propX(varX("this"), field)
            final Expression current = callX(HASHUTIL_TYPE, "updateHash", args(result, getter));
            fieldsBody.addStatement(assignS(result, current))
        }

        final BlockStatement idBody = new BlockStatement();
        Expression getter = callThisX('getId')
        final Expression current = callX(HASHUTIL_TYPE, "updateHash", args(result, getter));
        idBody.addStatement(assignS(result, current))

        if( includes ) {
            body.addStatement(
                ifElseS(equalsNullX(callThisX('getId')), fieldsBody, idBody)
            )
        } else {
            body.addStatement(idBody)
        }

        // methodBody.addStatement(body);

        cNode.addMethod(new MethodNode(
            hasExistingHashCode ? "_hashCode" : "hashCode",
            hasExistingHashCode ? ACC_PRIVATE : ACC_PUBLIC,
            ClassHelper.int_TYPE,
            Parameter.EMPTY_ARRAY,
            ClassNode.EMPTY_ARRAY,
            body));
    }

    static void createEqualsForId(ClassNode cNode, List<String> includes) {
        // if (useCanEqual) createCanEqual(cNode);
        // make a public method if none exists otherwise try a private method with leading underscore
        boolean hasExistingEquals = hasDeclaredMethod(cNode, "equals", 1);
        if (hasExistingEquals && hasDeclaredMethod(cNode, "_equals", 1)) return;

        final BlockStatement body = new BlockStatement();
        VariableExpression other = varX("other");

        // some short circuit cases for efficiency
        body.addStatement(ifS(equalsNullX(other), returnS(constX(Boolean.FALSE, true))));
        body.addStatement(ifS(sameX(varX("this"), other), returnS(constX(Boolean.TRUE, true))));

        body.addStatement(ifS(notX(isInstanceOfX(other, GenericsUtils.nonGeneric(cNode))), returnS(constX(Boolean.FALSE, true))));

        VariableExpression otherTyped = varX("otherTyped", GenericsUtils.nonGeneric(cNode));
        CastExpression castExpression = new CastExpression(GenericsUtils.nonGeneric(cNode), other);
        castExpression.setStrict(true);
        body.addStatement(declS(otherTyped, castExpression));

        // if (useCanEqual) {
        //     body.addStatement(ifS(notX(callX(otherTyped, "canEqual", varX("this"))), returnS(constX(Boolean.FALSE,true))));
        // }

        List<PropertyNode> pList = getInstanceProperties(cNode);
        String getterName = 'getId'
        def eqexp = eqX(callThisX(getterName), callX(otherTyped, getterName))
        //body.addStatement(ifS(notX(eqexp), returnS(constX(Boolean.FALSE, true))));
        body.addStatement(
            ifS(
                andX( notX(equalsNullX(callThisX(getterName))) , notX(equalsNullX(callX(otherTyped, getterName)))),
                returnS(eqexp)
            )
        )
                // default
        body.addStatement(returnS(constX(Boolean.FALSE, true)));

        cNode.addMethod(new MethodNode(
            hasExistingEquals ? "_equals" : "equals",
            hasExistingEquals ? ACC_PRIVATE : ACC_PUBLIC,
            ClassHelper.boolean_TYPE,
            params(param(OBJECT_TYPE, other.getName())),
            ClassNode.EMPTY_ARRAY,
            body));
    }
}
