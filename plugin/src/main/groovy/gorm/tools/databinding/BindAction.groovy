package gorm.tools.databinding

import groovy.transform.CompileStatic

@CompileStatic
@SuppressWarnings("FieldName")
enum BindAction {
    Create, Update, Delete, Generic
}
