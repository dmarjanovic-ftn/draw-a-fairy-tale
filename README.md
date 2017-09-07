# Draw a Fairy Tale
Bachelor of Computer Science on thesis - Big Data Mining with Scala and Spark

## Running
As you know, Spark has support to different cluster managers such as:

- Hadoop YARN
- Apache Mesos
- Spark Standalone Cluster

This represents another layer for application running, so I decide to use easiest solution - **Spark Standalone Cluster**.

The main idea is to run **master** with 4 **slaves/workers**. Each **worker** will have subset of dataset locally.

### Setup Spark Standalone Cluster

We'll run it on single machine, but it shouldn't differs to running it on **real cluster**.
 
Everything you need to do is to download and setup **Java** and **Scala** to your machine. Then, you should repeat these steps:

1. Go to directory where your _Spark_ is installed 
2. Then run `./sbin/start-master.sh`

    You can add additional **flags** such as `PORT` but _default_ is okay for us. By default port `7077` will be used. Also you will be able to see your **master** along with **workers** at `http://localhost:8080` by _default_.
    
3. Now we need to start our **workers**

    - Add `export SPARK_WORKER_INSTANCES=4` to `conf/spark-env.sh` file if you want to run 4 workers.
    - Then run `./sbin/start-slave.sh $MASTER` where `MASTER` represents **Spark master**

    If you check again at `http://localhost:8080` you'll able to see your **workers** also. You can add special _flags_ such as `MEMORY`, `CORES_TO_USE`, etc. But, let's stick to _default_ again.
    
4. Also, you can run `./bin/stop-all.sh` to **stop** both _master_ and _slaves_ and `./bin/start-all.sh` to start both _master_ and _slaves_.

### Run Scala Spark Application in Cluster

Once we setup **Standalone Cluster** we can run our application. 

The most common way to run your application in cluster is to fire `spark-submit` command. But it requires to run many commands such as `sbt assembly` and so on...

But, there's a way to run it without writing all these commands. We can use `sbt-spark-submit` plugin which makes us easier running Spark jobs.

After _simple configuration_ based from [official GitHub project](https://github.com/saurfang/sbt-spark-submit) I simplified running on *Standalone Spark Cluster* to the following command - `sbt spark` which implicitly sets following parameters:

- `--class Main`, which represents `main` class in applicaiton
- `--master spark://dmarjanovic-mbp.local:7077` which represents **URL** to **Spark Master**
