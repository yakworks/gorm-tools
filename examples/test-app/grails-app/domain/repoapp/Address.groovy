package repoapp

import testing.DropZone

class Address {
    String city
    Long testId
    DropZone dropZone
    static constraints = {
        testId nullable: true
        dropZone nullable: true
    }
}
