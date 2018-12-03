# `Java Chaincode` 开发教程

本教程将讲解如何编写基于`Java`的`Hyperledger Fabric`链代码。有关链码的一般说明，如何编写和操作，请访问[Chaincode教程](https://github.com/hooj0/notes/blob/master/blockchain/hyperledger/hyperledger%20fabric%20Chaincode%20%E5%BC%80%E5%8F%91%E6%95%99%E7%A8%8B.md)。

# 必要工具

+ `JDK8+`
+ `Gradle 4.8+`
+ `Hyperledger Fabric 1.3+`

# 环境配置

这里主要是配置 `Gradle` 的环境变量。

```sh
export path=path:/opt/gradle-4.8
```

# 简单的`Chaincode`开发示例

编写自己的链代码需要了解`Fabric`平台，`Java`和`Gradle`。应用程序是一个基本的示例链代码，用于在分类帐上创建资产（键值对）。

## 下载代码

```sh
$ git clone https://github.com/hyperledger/fabric-chaincode-java.git
```

在开发工具`eclipse`中导入工程代码。

**文件夹结构**：

+ `fabric-chaincode-protos` 文件夹包含`Java shim`用于与`Fabric`对等方通信的`protobuf`定义文件。

+ `fabric-chaincode-shim` 文件夹包含定义`Java`链代码`API`的`java shim`类以及与`Fabric`对等方通信的方式。

+ `fabric-chaincode-docker` 文件夹包含构建`docker`镜像的说明 `hyperledger/fabric-javaenv`。

+ `fabric-chaincode-example-gradle` 包含一个示例`java chaincode gradle`项目，其中包含示例链代码和基本`gradle`构建指令。

## 创建`Gradle`项目

**可以使用`fabric-chaincode-example-gradle`作为起始点。或者在`fabric-chaincode-example-gradle`的同级新建一个`gradle`项目`fabric-chaincode-asset-gradle`**。确保项目构建创建一个可运行的`jar`，其中包含名为`chaincode.jar`的所有依赖项。

```groovy
plugins {
    id 'com.github.johnrengelman.shadow' version '2.0.3'
    id 'java'
}

group 'org.hyperledger.fabric-chaincode-java'
version '1.3.1-SNAPSHOT'

sourceCompatibility = 1.8

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compile group: 'org.hyperledger.fabric-chaincode-java', name: 'fabric-chaincode-shim', version: '1.3.+'
    testCompile group: 'junit', name: 'junit', version: '4.12'
}

shadowJar {
    baseName = 'chaincode'
    version = null
    classifier = null

    manifest {
        attributes 'Main-Class': 'com.github.hooj0.chaincode.SimpleAssetChaincode'
    }
}
```

**新建完成后，可以在项目上右键，选择`Gradle -> Refresh Gradle Project`加载项目依赖的`Jar`包**。

## 创建 `Maven` 项目

**可以在`fabric-chaincode-example-gradle`的同级新建一个`maven`项目`fabric-chaincode-asset-maven`**。确保项目构建创建一个可运行的`jar`，其中包含名为`chaincode.jar`的所有依赖项。在`maven`的配置文件`pom.xml`中添加如下配置：

```xml
<properties>
    <!-- Generic properties -->
    <java.version>1.8</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

    <!-- fabric-chaincode-java dev -->
    <fabric-chaincode-java.version>1.3.1-SNAPSHOT</fabric-chaincode-java.version>
    <!-- prod env
  <fabric-chaincode-java.version>1.3.0</fabric-chaincode-java.version>
  -->

    <!-- Logging -->
    <logback.version>1.0.13</logback.version>
    <slf4j.version>1.7.5</slf4j.version>

    <!-- Test -->
    <junit.version>4.11</junit.version>
</properties>

<dependencies>

    <!-- fabric-chaincode-java -->
    <dependency>
        <groupId>org.hyperledger.fabric-chaincode-java</groupId>
        <artifactId>fabric-chaincode-shim</artifactId>
        <version>${fabric-chaincode-java.version}</version>
        <scope>compile</scope>
    </dependency>

    <dependency>
        <groupId>org.hyperledger.fabric-chaincode-java</groupId>
        <artifactId>fabric-chaincode-protos</artifactId>
        <version>${fabric-chaincode-java.version}</version>
        <scope>compile</scope>
    </dependency>


    <!-- fabric-sdk-java -->

    <!-- Logging with SLF4J & LogBack -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>${slf4j.version}</version>
        <scope>compile</scope>
    </dependency>

    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>${logback.version}</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Test Artifacts -->
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>${junit.version}</version>
        <scope>test</scope>
    </dependency>

</dependencies>

<build>
    <sourceDirectory>src/main/java</sourceDirectory>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.1</version>
            <configuration>
                <source>${java.version}</source>
                <target>${java.version}</target>
            </configuration>
        </plugin>

        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.1.0</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <finalName>chaincode</finalName>
                        <transformers>
                            <transformer
                                         implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <mainClass>com.github.hooj0.chaincode.SimpleAssetChaincode</mainClass>
                            </transformer>
                        </transformers>
                        <filters>
                            <filter>
                                <!-- filter out signature files from signed dependencies, else repackaging fails with security ex -->
                                <artifact>*:*</artifact>
                                <excludes>
                                    <exclude>META-INF/*.SF</exclude>
                                    <exclude>META-INF/*.DSA</exclude>
                                    <exclude>META-INF/*.RSA</exclude>
                                </excludes>
                            </filter>
                        </filters>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## 创建链码类

使用`Java`版的[`Simple Asset Chaincode`](https://hyperledger-fabric.readthedocs.io/en/latest/chaincode4ade.html#simple-asset-chaincode)作为示例。这个链代码是`Simple Asset Chaincode`的`Go to Java`翻译，将对此进行解释。

`ChaincodeBase`类是一个**抽象类**，它继承了`Chaincode`形式，它包含用于启动`chaincode`的`start`方法。因此，将通过扩展`ChaincodeBase`而不是实现`Chaincode`来创建我们的链代码。

首先，从一些基本的开始，创建一个`class`文件`SimpleAssetChaincode`。与每个链代码一样，它**继承了[`ChaincodeBase`抽象类](https://godoc.org/github.com/hyperledger/fabric/core/chaincode/shim#Chaincode)，特别是实现了`init`和`invoke`函数**。

```java
package com.github.hooj0.chaincode;

import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;

/**
 * simple asset chaincode
 * @author hoojo
 * @createDate 2018年11月30日 下午4:13:27
 * @file SimpleAssetChaincode.java
 * @package com.github.hooj0.chaincode
 * @project fabric-chaincode-asset-gradle
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class SimpleAssetChaincode extends ChaincodeBase {

}
```

## 初始化`Chaincode`

接下来，将**实现`init`函数**。

```java
    /**
     * Init is called during chaincode instantiation to initialize any
     * data. Note that chaincode upgrade also calls this function to reset
     * or to migrate data.
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @return response
     */
    @Override
    public Response init(ChaincodeStub stub) {       
    }
```

> **注意**：链码**升级也会调用此函数**。在编写将升级现有链代码的链代码时，请确保正确修改`Init`函数。特别是，如果**没有“迁移”或者在升级过程中没有任何内容要初始化，请提供一个空的`Init`方法**。

接下来，将**使用`ChaincodeStub -> stub.getStringArgs`函数检索`Init`调用的参数并检查其有效性**。在例子中，将使用一个键值对。`Chaincode`初始化在`Response init(ChaincodeStub stub)`方法内完成。首先，使用`ChaincodeStub.getStringArgs()`方法获取参数。

```go
    /**
     * Init is called during chaincode instantiation to initialize any
     * data. Note that chaincode upgrade also calls this function to reset
     * or to migrate data.
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @return response
     */
    @Override
    public Response init(ChaincodeStub stub) {
        // Get the args from the transaction proposal
        List<String> args = stub.getStringArgs();
        if (args.size() != 2) {
            newErrorResponse("Incorrect arguments. Expecting a key and a value");
        }
        return newSuccessResponse();
    }
```

接下来，既然已经确定调用有效，将**把初始状态存储在分类帐中**。为此，将调用`stub.putStringState`作为参数传入的键和值。假设一切顺利，返回一个`Response`对象，表明初始化成功。

```java
    /**
     * Init is called during chaincode instantiation to initialize any
     * data. Note that chaincode upgrade also calls this function to reset
     * or to migrate data.
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @return response
     */
    @Override
    public Response init(ChaincodeStub stub) {
        try {
            // Get the args from the transaction proposal
            List<String> args = stub.getStringArgs();
            if (args.size() != 2) {
                newErrorResponse("Incorrect arguments. Expecting a key and a value");
            }
            // Set up any variables or assets here by calling stub.putState()
            // We store the key and the value on the ledger
            stub.putStringState(args.get(0), args.get(1));
            return newSuccessResponse();
        } catch (Throwable e) {
            return newErrorResponse("Failed to create asset");
        }
    }
```

## 调用`Chaincode`

首先，**添加`invoke`函数的签名**。`Chaincode`调用在`Response invoke(ChaincodeStub stub)`方法内完成。

```java
    /**
     * Invoke is called per transaction on the chaincode. Each transaction is
     * either a 'get' or a 'set' on the asset created by Init function. The Set
     * method may create a new asset by specifying a new key-value pair.
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @return response
     */
    @Override
    public Response invoke(ChaincodeStub stub) {
        return newSuccessResponse();
    }
```

与上面的`init`函数一样，需要**从`ChaincodeStub`中提取参数**。**`invoke`函数的参数将是要调用的链代码应用程序函数的名称**。在例子中，应用程序将只有两个函数：`set`和`get`，它们允许设置资产的值或检索其当前状态。使用`ChaincodeStub.getFunction()`和`ChaincodeStub.getParameters()`方法**提取函数名称和参数**。**验证函数名称**并**调用相应的链代码方法**。**链代码方法接收的值**应作为**成功响应**有效负载**返回**。如果出现**异常或不正确**的函数值，则**返回错误响应**。

```java
    public Response invoke(ChaincodeStub stub) {
        try {
            // Extract the function and args from the transaction proposal
            String func = stub.getFunction();
            List<String> params = stub.getParameters();            
        }
    }
```

接下来，将函数名称验证为`set`或`get`，并调用这些链代码应用程序函数，通过调用父类的`newSuccessResponse`或`newErrorResponse`函数返回适当的响应，这些函数将**响应序列化为`gRPC protobuf`消息**。

```java
	public Response invoke(ChaincodeStub stub) {
        try {
            // Extract the function and args from the transaction proposal
            String func = stub.getFunction();
            List<String> params = stub.getParameters();
            if (func.equals("set")) {
                // Return result as success payload
                return newSuccessResponse(set(stub, params));
            } else if (func.equals("get")) {
                // Return result as success payload
                return newSuccessResponse(get(stub, params));
            }
            return newErrorResponse("Invalid invoke function name. Expecting one of: [\"set\", \"get\"");
        } catch (Throwable e) {
            return newErrorResponse(e.getMessage());
        }
    }
```

## 实现`Chaincode`应用程序

如上所述，链码应用程序实现了两个可以通过`invoke`函数调用的函数，现在实现这些功能。请注意，正如上面提到的，为了**访问分类帐的状态**，使用`ChaincodeStub.putStringState(key，value)`和`ChaincodeStub.getStringState(key)`实现方法`set()`和`get()`。

```java
	/**
     * get returns the value of the specified asset key
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @param args key
     * @return value
     */
    private String get(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1) {
            throw new RuntimeException("Incorrect arguments. Expecting a key");
        }

        String value = stub.getStringState(args.get(0));
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Asset not found: " + args.get(0));
        }
        return value;
    }

    /**
     * set stores the asset (both key and value) on the ledger. If the key exists,
     * it will override the value with the new one
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @param args key and value
     * @return value
     */
    private String set(ChaincodeStub stub, List<String> args) {
        if (args.size() != 2) {
            throw new RuntimeException("Incorrect arguments. Expecting a key and a value");
        }
        stub.putStringState(args.get(0), args.get(1));
        return args.get(1);
    }
```

## 示例完整代码

最后，需要添加`main`函数，它将调用`shim.Start`函数。这是整个链码程序的完整源代码文件。

```java
package com.github.hooj0.chaincode;

import java.util.List;

import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;

/**
 * simple asset chaincode
 * @author hoojo
 * @createDate 2018年11月30日 下午4:13:27
 * @file SimpleAssetChaincode.java
 * @package com.github.hooj0.chaincode
 * @project fabric-chaincode-asset-gradle
 * @blog http://hoojo.cnblogs.com
 * @email hoojo_@126.com
 * @version 1.0
 */
public class SimpleAssetChaincode extends ChaincodeBase {

	/**
     * Init is called during chaincode instantiation to initialize any
     * data. Note that chaincode upgrade also calls this function to reset
     * or to migrate data.
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @return response
     */
    @Override
    public Response init(ChaincodeStub stub) {
        try {
            // Get the args from the transaction proposal
            List<String> args = stub.getStringArgs();
            if (args.size() != 2) {
                newErrorResponse("Incorrect arguments. Expecting a key and a value");
            }
            // Set up any variables or assets here by calling stub.putState()
            // We store the key and the value on the ledger
            stub.putStringState(args.get(0), args.get(1));
            return newSuccessResponse();
        } catch (Throwable e) {
            return newErrorResponse("Failed to create asset");
        }
    }

    /**
     * Invoke is called per transaction on the chaincode. Each transaction is
     * either a 'get' or a 'set' on the asset created by Init function. The Set
     * method may create a new asset by specifying a new key-value pair.
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @return response
     */
    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            // Extract the function and args from the transaction proposal
            String func = stub.getFunction();
            List<String> params = stub.getParameters();
            if (func.equals("set")) {
                // Return result as success payload
                return newSuccessResponse(set(stub, params));
            } else if (func.equals("get")) {
                // Return result as success payload
                return newSuccessResponse(get(stub, params));
            }
            return newErrorResponse("Invalid invoke function name. Expecting one of: [\"set\", \"get\"");
        } catch (Throwable e) {
            return newErrorResponse(e.getMessage());
        }
    }

    /**
     * get returns the value of the specified asset key
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @param args key
     * @return value
     */
    private String get(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1) {
            throw new RuntimeException("Incorrect arguments. Expecting a key");
        }

        String value = stub.getStringState(args.get(0));
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Asset not found: " + args.get(0));
        }
        return value;
    }

    /**
     * set stores the asset (both key and value) on the ledger. If the key exists,
     * it will override the value with the new one
     *
     * @param stub {@link ChaincodeStub} to operate proposal and ledger
     * @param args key and value
     * @return value
     */
    private String set(ChaincodeStub stub, List<String> args) {
        if (args.size() != 2) {
            throw new RuntimeException("Incorrect arguments. Expecting a key and a value");
        }
        stub.putStringState(args.get(0), args.get(1));
        return args.get(1);
    }

    public static void main(String[] args) {
        new SimpleAssetChaincode().start(args);
    }
}
```

## 构建`Chaincode`

现在编译构建链码。

```sh
$ cd fabric-chaincode-asset/fabric-chaincode-asset-gradle

$ gradle clean build shadowJar

# 如果是Maven项目
$ mvn clean install
```

假设没有错误，会生成`jar`文件，现在可以继续下一步，测试链代码。

```sh
$ ll build/libs/
total 16584
-rw-r--r-- 1 Administrator 197121 16974993 十一 30 17:56 chaincode.jar
-rw-r--r-- 1 Administrator 197121     2371 十一 30 17:56 fabric-chaincode-asset-gradle-1.3.1-SNAPSHOT.jar
```

## 使用开发模式测试

通常，**链码由对等体启动和维护**。然而，在“**开发模式**”中，**链码由用户构建和启动**。在链码开发阶段，此模式非常有用，可用于**快速代码/构建/运行/调试**周期周转。

通过为示例开发网络，利用**预先生成的定序者和通道工件**来启动“开发模式(`dev mode`)”。这样，用户可以**立即进入编译链码和操作调用**的过程。

如果还不会开发模式，可以参考[开发模式使用方式文档](https://github.com/hooj0/notes/blob/master/blockchain/hyperledger/hyperledger%20fabric%20Chaincode%20%E5%BC%80%E5%8F%91%E6%A8%A1%E5%BC%8F.md)。

# 准备`Gradle`示例和链码

如果还没有这样做，请[安装样本，二进制文件和Docker镜像](https://hyperledger-fabric.readthedocs.io/en/latest/install.html)。

进入到示例目录下的 `chaincode`位置。

```sh
$ cd fabric-samples/chaincode
```

创建链码文件夹

```sh
$ mkdir -p asset/java
```

将项目的**源代码和`gradle`**的配置文件，拷贝到上面的文件夹中

```sh
$ cp xxx/xxx/src/*.java asset/java/src

$ cp xxx/xxx/*.gradle asset/java/
```
# 准备`Maven`示例和链码

如果还没有这样做，请[安装样本，二进制文件和Docker镜像](https://hyperledger-fabric.readthedocs.io/en/latest/install.html)。

进入到示例目录下的 `chaincode`位置。

```sh
$ cd fabric-samples/chaincode
```

创建链码文件夹

```sh
$ mkdir -p asset/java
```

将项目的**源代码和`maven`**的配置文件，拷贝到上面的文件夹中

```sh
$ cp -r xxx/xxx/src asset/java/

$ cp xxx/xxx/*.pom asset/java/
```

# 测试和运行链码

进入到`fabric-samples`项目的`chaincode-docker-devmode`目录：

```sh
$ cd chaincode-docker-devmode
```

现在打开三个终端并导航到每个终端中的`chaincode-docker-devmode`目录。

## 终端1 - 启动网络

```sh
$ docker-compose -f docker-compose-simple.yaml up
```

以上内容**使用`SingleSampleMSPSolo`定序者配置文件**启动网络，并以“**开发模式**”启动对等体。它还启动了两个额外的容器，一个**用于链码环境**，另一个**用于与链代码交互**。创建和加入通道的命令嵌入在`CLI`容器中，因此可以立即跳转到链代码调用。

## 终端2  - 使用链码

即使处于`--peer-chaincodedev`模式，仍然**必须安装链代码**，以便**生命周期系统链码可以正常进行检查**。在`--pere-chaincodedev`模式下，将来可能会删除此要求。

```sh
$ docker exec -it cli bash
```

在 `cli` 容器中运行：

```sh
$ peer chaincode install -n asset -v v0 -l java -p /opt/gopath/src/chaincodedev/chaincode/asset/java/

$ peer chaincode instantiate -n asset -v v0 -c '{"Args":["a", "5"]}' -C myc -l java
```

现在发出一个`invoke`调用，将`a`的值更改为“`20`”。

```sh
$ peer chaincode invoke -n asset -c '{"Args":["set", "a", "20"]}' -C myc
```

最后，查询`a`。应该看到`20`的值。

```sh
$ peer chaincode query -n asset -c '{"Args":["get","a"]}' -C myc
```

## 测试新的链码

默认情况下，只挂载`sacc`。但是，可以通过将不同的**链码添加到`chaincode`子目录**并**重新启动网络来轻松地测试它们**。此时，可以在`chaincode`容器中访问它们。

