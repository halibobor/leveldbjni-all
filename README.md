#NOTE
this repo is fork from [leveldbjni](https://github.com/fusesource/leveldbjni)

#Build leveldb
```shell
wget https://github.com/google/snappy/releases/download/1.1.4/snappy-1.1.4.tar.gz
tar -zxvf snappy-1.1.4.tar.gz
git clone https://github.com/chirino/leveldb.git
git clone -b dev https://github.com/halibobor/leveldbjni.git

export SNAPPY_HOME=`cd snappy-1.1.4; pwd`
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
git apply ../leveldbjni/arm64.patch
git apply ../leveldbjni/MIPS.patch
make libleveldb.a
```

   

#Build jni
```shell
 cd ${LEVELDBJNI_HOME}
 mvn clean install -P platform
```
