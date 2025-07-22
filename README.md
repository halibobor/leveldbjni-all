#NOTE
this repo is fork from [leveldbjni](https://github.com/fusesource/leveldbjni)

#Build
```shell
wget https://src.fedoraproject.org/lookaside/pkgs/snappy/snappy-1.0.5.tar.gz/4c0af044e654f5983f4acbf00d1ac236/snappy-1.0.5.tar.gz

tar -zxvf snappy-1.0.5.tar.gz

git clone git@github.com:chirino/leveldb.git
git clone -b release_1.18.4 https://github.com/halibobor/leveldbjni.git


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
brew install maven autoconf automake libtool
set $JAVA_HOME
```
    

#Build jni
```shell
cd ${LEVELDBJNI_HOME}
 mvn clean install -P osx-aarch64
```
## build err for mac
```
[INFO] configure: error: cannot find required auxiliary files: compile
[INFO] make: *** [config.status] Error 1
```
### Solution
```shell
cd ${LEVELDBJNI_HOME}/leveldbjni-osx-aarch64/target/native-build
automake --add-missing --copy --force-missing
autoreconf -fiv
cd ${LEVELDBJNI_HOME}
mvn  install -P osx-aarch64
```
