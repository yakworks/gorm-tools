/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.excel.render

import groovy.transform.CompileStatic

import builders.dsl.spreadsheet.api.FontStyle
import builders.dsl.spreadsheet.builder.api.CanDefineStyle
import builders.dsl.spreadsheet.builder.api.Stylesheet

@CompileStatic
class BookExcelStylesheet implements Stylesheet {
    public static final String STYLE_HEADER = "header"

    @Override
    void declareStyles(CanDefineStyle stylable) {
        stylable.style(STYLE_HEADER) { st ->
            st.font { f -> f.style(FontStyle.BOLD) }
        }
    }
}
