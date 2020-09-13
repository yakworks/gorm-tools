/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.audit.ast

import java.lang.reflect.Modifier

import groovy.transform.CompilationUnitAware
import groovy.transform.CompileStatic

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.VariableScope
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.grails.compiler.injection.GrailsASTUtils
import org.grails.datastore.mapping.reflect.AstUtils

import gorm.tools.security.audit.AuditStampTrait

import static org.codehaus.groovy.ast.MethodNode.ACC_PUBLIC
import static org.codehaus.groovy.ast.MethodNode.ACC_STATIC

/**
 * Performs an ast transformation on a class - adds createdBy/createdDate editedBy/EditedDate
 * properties to the subject class.
 */
@CompileStatic
@SuppressWarnings(['ThrowRuntimeException', 'CatchThrowable'])
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS) //use SEMANTIC_ANALYSIS so its picked up before the gorm ones
class AuditStampASTTransformation implements ASTTransformation, CompilationUnitAware  {
    private static final ClassNode MY_TYPE = new ClassNode(gorm.tools.security.audit.AuditStamp)
    private ConfigObject stampCfg

    AuditStampASTTransformation() {
        stampCfg = new AuditStampConfigLoader().load()
    }

    private CompilationUnit unit

    // implements CompilationUnitAware
    void setCompilationUnit(CompilationUnit unit) {
        this.unit = unit
    }

    void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        // println("stampCfg: " + stampCfg)
        Boolean enabled = (Boolean) FieldProps.getMap(stampCfg, FieldProps.CONFIG_KEY + "." + "enabled")
        //println("AuditStampAST enabled: " + enabled)
        if (enabled != null && enabled == false) return

        if (!(astNodes[0] instanceof AnnotationNode) || !(astNodes[1] instanceof ClassNode)) {
            throw new RuntimeException('Internal error: wrong types: $node.class / $parent.class')
        }

        ClassNode classNode = (ClassNode) astNodes[1]
        // println("AuditStampAST classNode: " + classNode)
        AnnotationNode annotationNode = (AnnotationNode) astNodes[0]
        if (MY_TYPE != annotationNode.getClassNode()) {
            return
        }
        final ast = sourceUnit.getAST()
        Map<String, FieldProps> fprops = FieldProps.buildFieldMap(stampCfg)

        Boolean useAuditStampTrait = (Boolean) FieldProps.getMap(stampCfg, FieldProps.CONFIG_KEY + "." + "useAuditStampTrait")

        if (useAuditStampTrait != null && useAuditStampTrait == false) {
            addDisableAuditStampField(classNode)
            //debugFieldNodes(classNode)
            createUserField(classNode, fprops.get(FieldProps.EDITED_BY_KEY))
            createUserField(classNode, fprops.get(FieldProps.CREATED_BY_KEY))

            createDateField(classNode, fprops.get(FieldProps.EDITED_DATE_KEY))
            createDateField(classNode, fprops.get(FieldProps.CREATED_DATE_KEY))
        } else {
            //use the default trait
            AstUtils.injectTrait(classNode, AuditStampTrait)
            addConstraints(classNode)
            // addImportFrom(classNode)
            // addMappingAndConstraints(classNode, fprops.get(FieldProps.EDITED_BY_KEY))
            // addMappingAndConstraints(classNode, fprops.get(FieldProps.CREATED_BY_KEY))
            // addMappingAndConstraints(classNode, fprops.get(FieldProps.EDITED_DATE_KEY))
            // addMappingAndConstraints(classNode, fprops.get(FieldProps.CREATED_DATE_KEY))

            // add fields too so the constraints can be added instead of messing with importFrom
            // createUserField(classNode, fprops.get(FieldProps.EDITED_BY_KEY))
            // createUserField(classNode, fprops.get(FieldProps.CREATED_BY_KEY))
            // createDateField(classNode, fprops.get(FieldProps.EDITED_DATE_KEY))
            // createDateField(classNode, fprops.get(FieldProps.CREATED_DATE_KEY))
        }
    }

    // using AstUtils.injectTrait(classNode, AuditStampTrait) directly now
    boolean addTrait(ClassNode classNode, String traitClassName) {
        //String auditStampTrait = traitClassName ?: 'gorm.tools.security.audit.AuditStampTrait'
        //def trtClass = getClass().classLoader.loadClass(auditStampTrait)
        // AstUtils.injectTrait(classNode, AuditStampTrait)
        boolean traitsAdded = false;
        boolean implementsTrait = false;
        boolean traitNotLoaded = false;
        // classNode.getModule().addImport("AuditStampTrait", ClassHelper.make("gorm.tools.security.audit.AuditStampTrait"))
        // // ClassNode traitClassNode = ClassHelper.make(getClass().classLoader.loadClass('gorm.tools.security.audit.AuditStampTrait'))
        // ClassNode traitClassNode = ClassHelper.make('AuditStampTrait')
        //
        ClassNode traitClassNode = new ClassNode(AuditStampTrait)
        try {
            implementsTrait = classNode.declaresInterface(traitClassNode);
        } catch (Throwable e) {
            // if we reach this point, the trait injector could not be loaded due to missing dependencies
            // (for example missing servlet-api). This is ok, as we want to be able to compile against non-servlet
            // environments.
            traitNotLoaded = true;
        }
        if (!implementsTrait && !traitNotLoaded) {
            final GenericsType[] genericsTypes = traitClassNode.getGenericsTypes();
            final parameterNameToParameterValue = [:] as Map<String, ClassNode>
            if(genericsTypes != null) {
                for(GenericsType gt : genericsTypes) {
                    parameterNameToParameterValue.put(gt.getName(), classNode);
                }
            }
            classNode.addInterface(GrailsASTUtils.replaceGenericsPlaceholders(traitClassNode, parameterNameToParameterValue, classNode));
            traitsAdded = true;
        }
        return traitsAdded;
    }

    public void addDisableAuditStampField(ClassNode classNode) {
        classNode.addField("disableAuditTrailStamp", Modifier.PUBLIC | Modifier.STATIC, new ClassNode(Boolean), ConstantExpression.FALSE)
    }

    public void createUserField(ClassNode classNode, FieldProps fieldProps) {
        if (fieldProps == null) return
        classNode.addProperty(fieldProps.name, Modifier.PUBLIC, new ClassNode(fieldProps.type), null, null, null)
        addMappingAndConstraints(classNode, fieldProps)
    }

    public void createDateField(ClassNode classNode, FieldProps fieldProps) {
        if (fieldProps == null) return
        classNode.addProperty(fieldProps.name, Modifier.PUBLIC, new ClassNode(fieldProps.type), null, null, null)
        addMappingAndConstraints(classNode, fieldProps)
    }

    public void addMappingAndConstraints(ClassNode classNode, FieldProps fieldProps) {
        //addSettings("mapping", classNode, fieldProps.name, fieldProps.mapping)
        addSettings("constraints", classNode, fieldProps.name, fieldProps.constraints)
    }

    public void addSettings(String name, ClassNode classNode, String fieldName, String config) {
        if (config == null) return

        String configStr = fieldName + " " + config

        BlockStatement newConfig = (BlockStatement) new AstBuilder().buildFromString(configStr).get(0)

        FieldNode closure = classNode.getField(name)
        if (closure == null) {
            createStaticClosure(classNode, name)
            closure = classNode.getField(name)
            assert closure != null
        }

        if (!hasFieldInClosure(closure, fieldName)) {
            ReturnStatement returnStatement = (ReturnStatement) newConfig.getStatements().get(0)
            ExpressionStatement exStatment = new ExpressionStatement(returnStatement.getExpression())
            ClosureExpression exp = (ClosureExpression) closure.getInitialExpression()
            BlockStatement block = (BlockStatement) exp.getCode()
            block.addStatement(exStatment)
        }

        assert hasFieldInClosure(closure, fieldName) == true
    }

    void addConstraints(ClassNode classNode) {
        String name = "constraints"
        String configStr = "AuditStampTraitConstraints(delegate)"
        def builder = new AstBuilder().buildFromString(configStr)

        BlockStatement newConfig = (BlockStatement) new AstBuilder().buildFromString(configStr).get(0)
        // ReturnStatement returnStatement = (ReturnStatement) newConfig.getStatements().get(0)
        // ExpressionStatement exStatment = new ExpressionStatement(returnStatement.getExpression())

        FieldNode closure = classNode.getField(name)
        ClosureExpression exp = (ClosureExpression) closure.getInitialExpression()
        BlockStatement block = (BlockStatement) exp.getCode()
        block.addStatement(newConfig)
        //block.statements.add(0, newConfig)
    }

    void addImportFrom(ClassNode classNode) {
        String name = "constraints"
        String configStr = "importFrom(AuditStampTraitConstraints)"

        //classNode.getModule().addImport("AuditStampTraitConstraints", new ClassNode(AuditStampTraitConstraints))

        //BlockStatement newConfig = (BlockStatement) new AstBuilder().buildFromString(configStr).get(0)

        // BlockStatement newConfig = (BlockStatement) new AstBuilder().buildFromString(configStr).get(0)
        //
        // FieldNode closure = classNode.getField(name)
        // ReturnStatement returnStatement = (ReturnStatement) newConfig.getStatements().get(0)
        // ExpressionStatement exStatment = new ExpressionStatement(returnStatement.getExpression())
        // ClosureExpression exp = (ClosureExpression) closure.getInitialExpression()
        // BlockStatement block = (BlockStatement) exp.getCode()
        // block.addStatement(exStatment)

        def constNode = ClassHelper.make(AuditStampTrait) //new ClassNode(AuditStampTraitConstraints)
        def ce = new ClassExpression(constNode)
        println "includes field ${constNode.getField('includes')}"
        //def incList = (ListExpression) constNode.getField("includes").getInitialExpression()
        def lexp = new ListExpression()
        // AuditStampTraitConstraints.props.each {
        //     lexp.addExpression(new ConstantExpression(it))
        // }
        //def incList = new FieldExpression(constNode.getField('includes'))
        assert lexp
        def me = new MapExpression()
        me.addMapEntryExpression(new ConstantExpression("include"), lexp)
        //def namedarg = new MapExpression(new ConstantExpression("include"), incList)
        assert me
        def arguments = new ArgumentListExpression(ce, me)
        println "arguments ${arguments}"
        //def arguments = new ArgumentListExpression(ce)
        //include: AuditStampTraitConstraints.includes

        def importFromMethodCall = new MethodCallExpression(new VariableExpression("this"), "importFrom", arguments)
        def methodCallStatement = new ExpressionStatement(importFromMethodCall)

        ClosureExpression exp = (ClosureExpression) classNode.getField(name).getInitialExpression()
        BlockStatement block = (BlockStatement) exp.getCode()
        block.addStatement(methodCallStatement)

        //classNode.getModule().addImport("AuditStampTraitConstraints", ClassHelper.make("gorm.tools.traits.AuditStampTraitConstraints"))

        //ReturnStatement returnStatement = (ReturnStatement) newConfig.getStatements().get(0)
        //ExpressionStatement exStatment = new ExpressionStatement(returnStatement.getExpression())
        // ClosureExpression exp = (ClosureExpression) closure.getInitialExpression()
        // BlockStatement block = (BlockStatement) exp.getCode()
        // block.addStatement(methodCallStatement)
    }

    void createStaticClosure(ClassNode classNode, String name) {
        FieldNode field = new FieldNode(name, ACC_PUBLIC | ACC_STATIC, new ClassNode(Object), new ClassNode(classNode.getClass()), null)
        ClosureExpression expr = new ClosureExpression(Parameter.EMPTY_ARRAY, new BlockStatement())
        expr.setVariableScope(new VariableScope())
        field.setInitialValueExpression(expr)
        classNode.addField(field)
    }

    boolean hasFieldInClosure(FieldNode closure, String fieldName) {
        if (closure != null) {
            ClosureExpression exp = (ClosureExpression) closure.getInitialExpression()
            BlockStatement block = (BlockStatement) exp.getCode()
            List<Statement> ments = block.getStatements()
            for (Statement expstat : ments) {
                if (expstat instanceof ExpressionStatement && ((ExpressionStatement) expstat).getExpression() instanceof MethodCallExpression) {
                    MethodCallExpression methexp = (MethodCallExpression) ((ExpressionStatement) expstat).getExpression()
                    ConstantExpression conexp = (ConstantExpression) methexp.getMethod()
                    if (conexp.getValue() == fieldName) {
                        return true
                    }
                }
            }
        }
        return false
    }

    void debugFieldNodes(ClassNode classNode) {
        List<PropertyNode> fnlist = classNode.getProperties()
        for (PropertyNode node : fnlist) {
            assert node
            //println(classNode.getName() + " : " + node.getName() + ",")
        }
    }

    //old but kept for reference
    /*
    public void addTableAndIdMapping(ClassNode classNode){
        FieldNode closure = classNode.getDeclaredField("mapping")

        if(closure!=null){
            boolean hasTable=hasFieldInClosure(closure,"table")
            boolean hasId=hasFieldInClosure(closure,"id")

            ClosureExpression exp = (ClosureExpression)closure.getInitialExpression()
            BlockStatement block = (BlockStatement) exp.getCode()

            //this just adds an s to the class name for the table if its not specified
            Boolean pluralize = (Boolean)getMap(CO,"stamp.mapping.pluralTable")
            if(!hasTable && pluralize!=null && pluralize){
                String tablename = GrailsClassUtils.getShortName(classNode.getName())+"s"
                //LOG.info("Added new mapping to assign table: " + tablename)
                MethodCallExpression tableMeth = new MethodCallExpression(
                    VariableExpression.THIS_EXPRESSION,
                    new ConstantExpression("table"),
                    new ArgumentListExpression(new ConstantExpression(tablename))
                    )
                //block = (BlockStatement) exp.getCode()
                block.addStatement(new ExpressionStatement(tableMeth))
                //System.out.println(classNode.getName()+" - Added table mapping " + tablename )
            }
            //This adds the ID generator that we use for domian classes
            Map tableconf = (Map)getMap(CO,"stamp.mapping.id")
            if(!hasId && tableconf!=null){
                NamedArgumentListExpression namedarg = new NamedArgumentListExpression()
                if(tableconf.get("column") != null){
                    namedarg.addMapEntryExpression(new ConstantExpression("column"), new ConstantExpression(tableconf.get("column").toString()))
                }
                if(tableconf.get("generator") != null){
                    namedarg.addMapEntryExpression(new ConstantExpression("generator"), new ConstantExpression(tableconf.get("generator").toString()))
                }
                MethodCallExpression tableMeth = new MethodCallExpression(
                    VariableExpression.THIS_EXPRESSION,
                    new ConstantExpression("id"),
                    namedarg
                    )
                //block = (BlockStatement) exp.getCode()
                block.addStatement(new ExpressionStatement(tableMeth))
                //System.out.println(classNode.getName() + " - Added ID mapping with "+ tableconf)
            }
        }
        */
    //System.out.println(block.toString())
}


//FUTURE
/**
 * java.math.BigDecimal
 * java.lang.Integer
 * java.lang.Long
 * java.util.Date
 * java.lang.String
 * java.lang.Boolean
 * <p>
 * since grails has everything default to nullable:false, we change that to nullable:true here since omost of the time we condider it ok
 * explicity set nullable:false as the exception
 * <p>
 * public void addConstraintDefaults(ClassNode classNode){
 * List<FieldNode>  fnlist = classNode.getFields()
 * for(FieldNode fnode : fnlist){
 * if(!fnode.isStatic()){
 * //check if the type is in our list
 * System.out.println("*" + fnode.getName() + " - " + fnode.getType().getName())
 * }
 * }
 * <p>
 * boolean hasConstraint=false
 * <p>
 * }
 * <p>
 * since grails has everything default to nullable:false, we change that to nullable:true here since omost of the time we condider it ok
 * explicity set nullable:false as the exception
 * <p>
 * public void addConstraintDefaults(ClassNode classNode){
 * List<FieldNode>  fnlist = classNode.getFields()
 * for(FieldNode fnode : fnlist){
 * if(!fnode.isStatic()){
 * //check if the type is in our list
 * System.out.println("*" + fnode.getName() + " - " + fnode.getType().getName())
 * }
 * }
 * <p>
 * boolean hasConstraint=false
 * <p>
 * }
 **/

/**
 since grails has everything default to nullable:false, we change that to nullable:true here since omost of the time we condider it ok
 explicity set nullable:false as the exception

 public void addConstraintDefaults(ClassNode classNode){
 List<FieldNode>  fnlist = classNode.getFields()
 for(FieldNode fnode : fnlist){
 if(!fnode.isStatic()){
 //check if the type is in our list
 System.out.println("*" + fnode.getName() + " - " + fnode.getType().getName())
 }
 }

 boolean hasConstraint=false

 }
 **/

/*
org.codehaus.groovy.ast.stmt.BlockStatement@f4b2da[
    org.codehaus.groovy.ast.stmt.ExpressionStatement@a0a4a[
        expression:org.codehaus.groovy.ast.expr.MethodCallExpression@29aa5a[
            object: org.codehaus.groovy.ast.expr.VariableExpression@6f0383[variable: this]
            method: ConstantExpression[discDate]
            arguments: org.codehaus.groovy.ast.expr.NamedArgumentListExpression@4fb195[
                org.codehaus.groovy.ast.expr.MapEntryExpression@13becc(key: ConstantExpression[nullable], value: ConstantExpression[true])
            ]
        ]
    ],.....

/*
{ org.codehaus.groovy.ast.stmt.BlockStatement@f0bc0[
    org.codehaus.groovy.ast.stmt.ExpressionStatement@cc9e15[
        expression:org.codehaus.groovy.ast.expr.MethodCallExpression@9e94e8[
            object: org.codehaus.groovy.ast.expr.VariableExpression@3c2282[variable: this]
            method: ConstantExpression[table]
            arguments: org.codehaus.groovy.ast.expr.ArgumentListExpression@42428a[ConstantExpression[SyncSteps]]
        ]
    ],
    org.codehaus.groovy.ast.stmt.ExpressionStatement@1eafb4[
        expression:org.codehaus.groovy.ast.expr.MethodCallExpression@a17663[
            object: org.codehaus.groovy.ast.expr.VariableExpression@3c2282[variable: this]
            method: ConstantExpression[id]
            arguments: org.codehaus.groovy.ast.expr.NamedArgumentListExpression@636202[
                org.codehaus.groovy.ast.expr.MapEntryExpression@b781ea(
                    key: ConstantExpression[column], value: ConstantExpression[OID]
                ),
                org.codehaus.groovy.ast.expr.MapEntryExpression@b25934(
                    key: ConstantExpression[generator], value: ConstantExpression[xx.hibernate.NewObjectIdGenerator]
                )
            ]
        ]
    ], org.codehaus.groovy.ast.stmt.ExpressionStatement@fe6f06[
        expression:org.codehaus.groovy.ast.expr.MethodCallExpression@2b0459[
            object: org.codehaus.groovy.ast.expr.VariableExpression@3c2282[variable: this]
            method: ConstantExpression[syncBatch]
            arguments: org.codehaus.groovy.ast.expr.NamedArgumentListExpression@2a938f[
                org.codehaus.groovy.ast.expr.MapEntryExpression@3dbf04(key: ConstantExpression[column], value: ConstantExpression[SyncBatchId])]]]] }


*/
