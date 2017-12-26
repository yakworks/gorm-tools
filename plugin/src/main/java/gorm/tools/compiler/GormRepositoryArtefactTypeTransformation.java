package gorm.tools.compiler;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.grails.compiler.injection.ArtefactTypeAstTransformation;

/**
 * A transformation that makes an Artefact a GormRepository
 */

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class GormRepositoryArtefactTypeTransformation extends ArtefactTypeAstTransformation {
    //Need to declare it here, because classes under java are compiled before groovy and does not have access to groovy classes in same app.
    private static final String TYPE = "Repository";

    @Override
    protected String resolveArtefactType(SourceUnit sourceUnit, AnnotationNode annotationNode, ClassNode classNode) {
        return TYPE;
    }

    @Override
    protected Class getAnnotationTypeClass() {
        return GormRepository.class;
    }
}
