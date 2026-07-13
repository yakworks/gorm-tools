package audit.test

import yakworks.security.auditable.Auditable

@SuppressWarnings("GroovyUnusedDeclaration")
class TestEntity implements Auditable {
  String property
  String otherProperty
  String anotherProperty

  // Just for testing
  Serializable ident() {
    "id"
  }

  @Override
  String toString() {
    property
  }
}
