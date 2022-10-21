package com.example.floatingview.manager;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Horrarndoo on 2022/9/1.
 * <p>
 * 线程管理者
 */
public class ThreadManager {
    private static ThreadPool mThreadPool;

    public static ThreadPool getThreadPool() {
        if (mThreadPool == null) {
            synchronized (ThreadManager.class) {
                if (mThreadPool == null) {
                    // 获取cpu数量
                    // int cpuCount = Runtime.getRuntime().availableProcessors();
                    // LogUtils.d("cup个数:" + cpuCount);
                    //线程个数
                    // int threadCount = cpuCount * 2 + 1;
                    int threadCount = 50;
                    mThreadPool = new ThreadPool(threadCount, threadCount, 1L);
                }
            }
        }
        return mThreadPool;
    }

    /**
     * 线程池
     */
    public static class ThreadPool {

        private final int corePoolSize;// 核心线程数
        private final int maximumPoolSize;// 最大线程数
        private final long keepAliveTime;// 休息时间

        private ThreadPoolExecutor executor;
        private final HashMap<Integer, Future<?>> futures;
        private final HashMap<Runnable, Integer> runnableIds;
        private int runnableId;

        @SuppressLint("UseSparseArrays")
        private ThreadPool(int corePoolSize, int maximumPoolSize,
                           long keepAliveTime) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.keepAliveTime = keepAliveTime;
            futures = new HashMap<>();
            runnableIds = new HashMap<>();
            runnableId = 0;
        }

        public void execute(Runnable r) {
            if (executor == null || executor.isShutdown()) {
                // 参1:核心线程数，除非allowCoreThreadTimeOut被设置为true，否则它闲着也不会死
                // 参2:最大线程数,活动线程数量超过它，后续任务就会排队  ;
                // 参3:超时时长，作用于非核心线程（allowCoreThreadTimeOut被设置为true时也会同时作用于核心线程），闲置超时便被回收 ;
                // 参4:枚举类型，设置keepAliveTime的单位;
                // 参5:缓冲任务队列，线程池的execute方法会将Runnable对象存储起来  ;
                // 参6:线程工厂接口，只有一个new Thread(Runnable r)方法，可为线程池创建新线程  ;
                // 参7:线程异常处理策略
                executor = new ThreadPoolExecutor(corePoolSize,
                        maximumPoolSize,
                        keepAliveTime,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<Runnable>(),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy());
            }

            // 线程池执行一个Runnable对象, 具体运行时机线程池说了算
            runnableId = generateRunnableId();
            futures.put(runnableId, executor.submit(r));
            runnableIds.put(r, runnableId);
        }

        /**
         * 取消正在执行的runnable
         *
         * @param r runnable
         */
        public void cancel(Runnable r) {
            if (r == null)
                return;

            if (executor != null) {
                try {
                    //终止正在执行的runnable
                    Integer rId = runnableIds.get(r);
                    Future<?> future = futures.get(rId);
                    if (future != null && rId != null) {
                        future.cancel(true);
                        futures.remove(rId);
                        runnableIds.remove(r);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 从线程队列中移除对象
                executor.remove(r);
                //                executor.getQueue().remove(r);
            }
        }

        /**
         * 取消所有的runnable
         */
        public void cancelAllRunnable() {
            for (Object o : futures.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                Future<?> future = (Future<?>) entry.getValue();
                future.cancel(true);
            }
            futures.clear();
        }

        //关闭线程池
        public void shutdown() {
            cancelAllRunnable();
            runnableIds.clear();
            runnableId = 0;
            executor.shutdown();
        }

        /**
         * 生成runnableId，避免runnaableId重复
         *
         * @return runnableId
         */
        private int generateRunnableId() {
            runnableId++;
            if (futures.containsKey(runnableId)) {
                return generateRunnableId();
            }
            return runnableId;
        }
    }
}
