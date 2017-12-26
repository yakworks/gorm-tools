package gorm.tools.databinding

/**
 * binds data from a map to a GormEntity. The map can of course be a JSONObject as is common when binding rest resources
 */
interface MapBinder {

    void bind(Object target, Map<String, Object> source, BindAction bindAction)
    void bind(Object target, Map<String, Object> source)

    void bind(Object target, Map<String, Object> source, BindAction bindAction, List<String> whiteList, List<String> blackList)
    void bind(Object target, Map<String, Object> source, List<String> whiteList, List<String> blackList)

    void bindCreate(Object target, Map<String, Object> source)
    void bindUpdate(Object target, Map<String, Object> source)

}
