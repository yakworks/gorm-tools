package yakworks.gorm.api.support;

/**
 * enum with the list of mime formats we support data.
 */
public enum DataMimeTypes {
    csv("text/csv"),
    json("application/json"),
    xlsx("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    public String mimeType;

    DataMimeTypes(String value) {
        this.mimeType = value;
    }

    @Override
    public String toString() {
        return name();
    }
}
