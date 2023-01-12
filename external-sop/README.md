<!--
SPDX-FileCopyrightText: 2021 Paul Schaub <vanitasvitae@fsfe.org>

SPDX-License-Identifier: Apache-2.0
-->

# External-SOP

Access an external SOP binary from within your Java/Kotlin application.

This module implements a backend for `sop-java` that binds to external SOP binaries (such as 
[sqop](https://gitlab.com/sequoia-pgp/sequoia-sop/), [python-sop](https://pypi.org/project/sop/) etc.).
SOP operation calls will be delegated to the external binary, and the results are parsed back, so that you can
access them from your Java application as usual.

## Example
Let's say you are using `ExampleSOP` which is a binary installed in `/usr/bin/example-sop`.
Instantiating a `SOP` object is as simple as this:

```java
SOP sop = new ExternalSOP("/usr/bin/example-sop");
```

This SOP object can now be used as usual (see [here](../sop-java/README.md)).

Some SOP binaries might require additional configuration, e.g. a Java based SOP might need to know which JAVA_HOME to use.
For this purpose, additional environment variables can be passed in using a `Properties` object:

```java
Properties properties = new Properties();
properties.put("JAVA_HOME", "/usr/lib/jvm/[...]");
SOP sop = new ExternalSOP("/usr/bin/example-sop", properties);
```

Most results of SOP operations are communicated via standard-out, standard-in. However, some operations rely on
writing results to additional output files.
To handle such results, we need to provide a temporary directory, to which those results can be written by the SOP,
and from which `External-SOP` reads them back.
The default implementation relies on `Files.createTempDirectory()` to provide a temporary directory.
It is however possible to overwrite this behavior, in order to specify a custom, perhaps more private directory:

```java
ExternalSOP.TempDirProvider provider = new ExternalSOP.TempDirProvider() {
    @Override
    public File provideTempDirectory() throws IOException {
        File myTempDir = new File("/path/to/directory");
        myTempDir.mkdirs();
        return myTempDir;
    }
};
SOP sop = new ExternalSOP("/usr/bin/example-sop", provider);
```
