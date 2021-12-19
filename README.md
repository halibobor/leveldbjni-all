# LevelDB JNI

## Description

LevelDB JNI gives you a Java interface to the 
[LevelDB](https://github.com/google/leveldb) C++ library
which is a fast key-value storage library written at Google 
that provides an ordered mapping from string keys to string values. 


##  Declaration

This LevelDB JNI is based on [leveldb-1.23](https://github.com/halibobor/leveldb/tree/leveldbjni/v1.23) and [snappy-1.1.8](https://github.com/halibobor/snappy/tree/leveldbjni/v1.1.8) .

Maybe you need this [LevelDB JNI](https://github.com/fusesource/leveldbjni) ?

## Getting the JAR

Just add the following jar to your java project:
[leveldbjni-all-v0.98.jar](https://jitpack.io/com/github/halibobo1205/leveldbjni-all/v0.98/leveldbjni-all-v0.98.jar)

## Using the Dependency 

Please refer to [leveldbjni-all/v0.98](https://jitpack.io/#halibobo1205/leveldbjni-all/v0.98) 

## API Usage:

Recommended Package imports:

```java 
    import org.iq80.leveldb.*;
    import static org.fusesource.leveldbjni.JniDBFactory.*;
    import java.io.*;
```

Opening and closing the database.

```java
    Options options = new Options();
    options.createIfMissing(true);
    DB db = factory.open(new File("example"), options);
    try {
      // Use the db in here....
    } finally {
      // Make sure you close the db to shutdown the 
      // database and avoid resource leaks.
      db.close();
    }
```
Putting, Getting, and Deleting key/values.

```java
    db.put(bytes("Tampa"), bytes("rocks"));
    String value = asString(db.get(bytes("Tampa")));
    db.delete(bytes("Tampa"));
```

Performing Batch/Bulk/Atomic Updates.

```java
    WriteBatch batch = db.createWriteBatch();
    try {
      batch.delete(bytes("Denver"));
      batch.put(bytes("Tampa"), bytes("green"));
      batch.put(bytes("London"), bytes("red"));

      db.write(batch);
    } finally {
      // Make sure you close the batch to avoid resource leaks.
      batch.close();
    }
```

Iterating key/values.

```java
    DBIterator iterator = db.iterator();
    try {
      for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
        String key = asString(iterator.peekNext().getKey());
        String value = asString(iterator.peekNext().getValue());
        System.out.println(key+" = "+value);
      }
    } finally {
      // Make sure you close the iterator to avoid resource leaks.
      iterator.close();
    }
```

Working against a Snapshot view of the Database.

```java
    ReadOptions ro = new ReadOptions();
    ro.snapshot(db.getSnapshot());
    try {
      
      // All read operations will now use the same 
      // consistent view of the data.
      ... = db.iterator(ro);
      ... = db.get(bytes("Tampa"), ro);

    } finally {
      // Make sure you close the snapshot to avoid resource leaks.
      ro.snapshot().close();
    }
```

Using a custom Comparator.

```java
    DBComparator comparator = new DBComparator(){
        public int compare(byte[] key1, byte[] key2) {
            return new String(key1).compareTo(new String(key2));
        }
        public String name() {
            return "simple";
        }
        public byte[] findShortestSeparator(byte[] start, byte[] limit) {
            return start;
        }
        public byte[] findShortSuccessor(byte[] key) {
            return key;
        }
    };
    Options options = new Options();
    options.comparator(comparator);
    DB db = factory.open(new File("example"), options);  
```
Disabling Compression.

```java
    Options options = new Options();
    options.compressionType(CompressionType.NONE);
    DB db = factory.open(new File("example"), options);
```

Configuring the Cache.

```java    
    Options options = new Options();
    options.cacheSize(100 * 1048576); // 100MB cache
    DB db = factory.open(new File("example"), options);
```

Configuring the BloomFilter.

```java    
    Options options = new Options();
    options.bitsPerKey(10);
    DB db = factory.open(new File("example"), options);
```

Getting approximate sizes.

```java
    long[] sizes = db.getApproximateSizes(new Range(bytes("a"), bytes("k")), new Range(bytes("k"), bytes("z")));
    System.out.println("Size: "+sizes[0]+", "+sizes[1]);
```
Getting database status.

```java
    String stats = db.getProperty("leveldb.stats");
    System.out.println(stats);
```

Getting database approximate-memory-usage.

```java
    String approximate = db.getProperty("leveldb.approximate-memory-usage");
    System.out.println(approximate);
```


Getting informational log messages.

```java
    Logger logger = new Logger() {
      public void log(String message) {
        System.out.println(message);
      }
    };
    Options options = new Options();
    options.logger(logger);
    DB db = factory.open(new File("example"), options);
```

Destroying a database.

```java    
    Options options = new Options();
    factory.destroy(new File("example"), options);
```

Repairing a database.

```java    
    Options options = new Options();
    factory.repair(new File("example"), options);
```

Using a memory pool to make native memory allocations more efficient:

```java
    JniDBFactory.pushMemoryPool(1024 * 512);
    try {
        // .. work with the DB in here, 
    } finally {
        JniDBFactory.popMemoryPool();
    }
```

## Building
> The following Steps work on mac, linux well, other platforms are not verified yet.
>
> Welcome to contribute to other platforms.
 
 ### Supported Platforms
 
 The following worked for me on:
 
  * OS X Mojave with X Code 11
  * OS X Monterey with X Code 13
  * CentOS 7.4 (64 bit)
  
     
### Prerequisites
*  GNU compiler toolchain,gcc-c++
*  JAVA(1.8+)，make sure the current session $JAVA_HOME is set.
*  [cmake](https://cmake.org/download/)
*  git
*  maven3
*  automake
*  autoconf
*  pkg-configls (linux maybe required)
*  libtool (linux maybe required)


### Build Procedure
Then download the snappy, leveldb, leveldbjni,leveldbjni-all project source code.

#### 1. snappy
```shell script
    git clone https://github.com/halibobor/snappy.git
    cd snappy
    git checkout leveldbjni/v1.1.8
    git submodule update --init
    mkdir build
    cd build && cmake ../ && make
    make DESTDIR=/tmp install
```
    
#### 2. leveldb
```shell script
    git clone https://github.com/halibobor/leveldb.git
    cd leveldb
    git checkout leveldbjni/v1.23
    git submodule update --init
    mkdir build
    export LIBRARY_PATH=/tmp/usr/local/lib # export LIBRARY_PATH=/tmp/usr/local/lib64
    export CPLUS_INCLUDE_PATH=/tmp/usr/local/include
    cd build && cmake -DCMAKE_BUILD_TYPE=Release .. && cmake --build .
    make DESTDIR=/tmp install
```
    
#### 3. leveldbjni
```shell script
    git clone https://github.com/halibobor/leveldbjni.git
    cd leveldbjni
    export SNAPPY_HOME=/tmp/usr/local
    export LEVELDB_HOME=/tmp/usr/local
    mvn clean install -P ${platform}
```
Replace ${platform} with one of the following platform identifiers (depending on the platform your building on):

* osx
* linux32
* linux64
* win32
* win64
* freebsd64

##### Build Results

* `leveldbjni-${platform}/target/native-build/target/lib/leveldbjni-${version}.[jnilib|so|dll]` : native library using your currently platform.
* `leveldbjni/target/leveldbjni-${version}-native-src.zip` : A GNU style source project which you can use to build the native library on other systems.
* `leveldbjni-${platform}/target/leveldbjni-${platform}-${version}.jar` : A jar file containing the built native library using your currently platform.


#### 4. leveldbjni-all
```shell script
    git clone https://github.com/halibobor/leveldbjni-all.git
    cd leveldbjni-all
    cd src/main/resources/META-INF/
    # mkdir for native library
    mkdir -p ${os}${bit-model}/${arch}
    # cp  native library  to ${os}${bit-model}/${arch}
    cp ${native_library_path}/leveldbjni-1.23.jnilib  ${os}${bit-model}/${arch}
    # update pom.xml
    # add new config to Bundle-NativeCode
    # such as META-INF/native/osx64/aarch64/libleveldbjni.jnilib;osname=macosx;processor=aarch64
    mvn clean install
```

> ${os},${bit-model},${arch} values, such as osx,64,x86_64.
>
> ${platform}=${os}${bit-model},such as osx64.
>
> Please refer to [Library.java](https://github.com/fusesource/hawtjni/blob/master/hawtjni-runtime/src/main/java/org/fusesource/hawtjni/runtime/Library.java) to get more.


### Build Results

* `leveldbjni/target/leveldbjni-all-${version}.jar` : The java class file to the library.