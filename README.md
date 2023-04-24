# JJson, a Java JSON library

Very unstable. Uses reflection to inspect your object, and then fill in the parsed json. Will throws errors if the json object being parsed and your object des not exactly match the field names and field types.

Supported parts of json (with nesting):
- Objects
- Arrays
- Strings
- Ints

Example object:
```code
public class TestObject {
    public String name;
    public int age;

    public TestObject() {}
}
```

Example Json:
```code
{
    "name": "Jimmy",
    "age": 24
}
```

Parsing:
```code
try { 
    TestObject obj = (TestObject) new JsonParser<TestObject>(json, TestObject.class).parse();
    System.out.println(obj);
} catch (InstantiationException | IllegalAccessException | InvocationTargetException | JsonException e) {
    e.printStackTrace();
}
```

Now you can use it like a regular object.

Deserialization:
```code
System.out.println(JsonUtils.toJsonString(obj))
```
