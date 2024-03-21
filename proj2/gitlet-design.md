# Gitlet Design Document

**Name**:

## Classes and Data Structures

### Class 1

#### Fields

1. Field 1
2. Field 2


### Class 2

#### Fields

1. Field 1
2. Field 2


## Algorithms

## Persistence

存储blob时,不做字符串切割,blob没有目录共用一个名字
存储blob时,ID号为saveFile的哈希值;这个哈希值也应该
是Commit和Stage映射的value,blob文件的名字也是这个saveFile的哈希值

