#NOTE
this repo is fork from [leveldbjni](https://github.com/fusesource/leveldbjni)

#Build
```shell
wget https://src.fedoraproject.org/lookaside/pkgs/snappy/snappy-1.0.5.tar.gz/4c0af044e654f5983f4acbf00d1ac236/snappy-1.0.5.tar.gz

tar -zxvf snappy-1.0.5.tar.gz

git clone git@github.com:chirino/leveldb.git
git clone git@github.com:fusesource/leveldbjni.git


export SNAPPY_HOME=`cd snappy-1.0.5; pwd`
export LEVELDB_HOME=`cd leveldb; pwd`
export LEVELDBJNI_HOME=`cd leveldbjni; pwd`

cd ${SNAPPY_HOME}
./configure --disable-shared --with-pic
make

cd ${LEVELDB_HOME}
export LIBRARY_PATH=${SNAPPY_HOME}
export C_INCLUDE_PATH=${LIBRARY_PATH}
export CPLUS_INCLUDE_PATH=${LIBRARY_PATH}
git apply ../leveldbjni/leveldb.patch
make libleveldb.a
```

#Special for M1
```shell script
brew install maven
set $JAVA_HOME
update pom.xml
        1. 1.5 -> 1.8 
        2. add java_home, remove with-universal
            <arg>--with-jni-jdk=${env.JAVA_HOME}</arg>
            <arg>--with-leveldb=${env.LEVELDB_HOME}</arg>
            <arg>--with-snappy=${env.SNAPPY_HOME}</arg>
            <!--<arg>--with-universal</arg>-->
```
    

#Build jni
```shell
 mvn clean install -P download -P osx
```
