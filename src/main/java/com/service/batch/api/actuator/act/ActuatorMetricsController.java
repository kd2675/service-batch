package com.service.batch.api.actuator.act;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Counter;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/service/batch/metrics")
@Slf4j
public class ActuatorMetricsController {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    private final String instanceId = getInstanceId();
    
    /**
     * MeterRegistry를 활용한 부하 정보 수집 (Spring Boot 3.x 호환)
     */
    @GetMapping("/load")
    public Map<String, Object> getLoadMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // 기본 정보
            metrics.put("instanceId", instanceId);
            metrics.put("timestamp", System.currentTimeMillis());
            
            // 시스템 메트릭 수집
            metrics.put("cpuUsage", getCpuUsage());
            metrics.put("memoryUsage", getMemoryUsage());
            metrics.put("activeThreads", getActiveThreads());
            metrics.put("responseTime", getAverageResponseTime());
            metrics.put("requestCount", getRequestCount());
            
            // 추가 시스템 정보
            metrics.put("systemLoadAverage", getSystemLoadAverage());
            metrics.put("availableProcessors", getAvailableProcessors());
            metrics.put("maxMemory", getMaxMemory());
            metrics.put("usedMemory", getUsedMemory());
            
            // 배치 관련 정보 (추후 구현)
            metrics.put("activeJobs", getActiveBatchJobs());
            metrics.put("queueSize", getBatchQueueSize());
            
            // 부하 점수 계산
            double loadScore = calculateLoadScore(metrics);
            metrics.put("loadScore", loadScore);
            metrics.put("isHealthy", isHealthy(metrics));
            
            log.debug("메트릭 수집 완료: {}, 부하점수: {:.2f}", instanceId, loadScore);
            
        } catch (Exception e) {
            log.error("메트릭 수집 실패: {}", e.getMessage(), e);
            // 실패 시 안전한 기본값 설정
            setDefaultMetrics(metrics);
        }
        
        return metrics;
    }
    
    /**
     * MeterRegistry를 통한 CPU 사용률 수집
     */
    private double getCpuUsage() {
        try {
            Gauge cpuGauge = meterRegistry.find("system.cpu.usage").gauge();
            if (cpuGauge != null) {
                double cpuValue = cpuGauge.value();
                return cpuValue * 100.0;
            }
        } catch (Exception e) {
            log.debug("CPU 사용률 MeterRegistry 수집 실패: {}", e.getMessage());
        }
        
        // Fallback: OperatingSystemMXBean 사용 (Java 8+)
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = 
                    (com.sun.management.OperatingSystemMXBean) osBean;
                return sunOsBean.getProcessCpuLoad() * 100.0;
            }
        } catch (Exception e) {
            log.debug("CPU 사용률 직접 수집 실패: {}", e.getMessage());
        }
        
        return 0.0;
    }
    
    /**
     * JVM 메모리 사용률 계산
     */
    private double getMemoryUsage() {
        try {
            // MeterRegistry에서 힙 메모리 정보 수집
            Gauge usedGauge = meterRegistry.find("jvm.memory.used")
                .tag("area", "heap")
                .gauge();
            Gauge maxGauge = meterRegistry.find("jvm.memory.max")
                .tag("area", "heap")
                .gauge();
            
            if (usedGauge != null && maxGauge != null) {
                double used = usedGauge.value();
                double max = maxGauge.value();
                if (max > 0) {
                    return (used / max) * 100.0;
                }
            }
        } catch (Exception e) {
            log.debug("메모리 사용률 MeterRegistry 수집 실패: {}", e.getMessage());
        }
        
        // Fallback: MemoryMXBean 직접 사용
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long used = memoryBean.getHeapMemoryUsage().getUsed();
            long max = memoryBean.getHeapMemoryUsage().getMax();
            
            if (max > 0) {
                return ((double) used / max) * 100.0;
            }
        } catch (Exception e) {
            log.debug("메모리 사용률 직접 수집 실패: {}", e.getMessage());
        }
        
        return 0.0;
    }
    
    /**
     * 활성 스레드 수 조회
     */
    private int getActiveThreads() {
        try {
            Gauge threadGauge = meterRegistry.find("jvm.threads.live").gauge();
            if (threadGauge != null) {
                return (int) threadGauge.value();
            }
        } catch (Exception e) {
            log.debug("스레드 수 MeterRegistry 수집 실패: {}", e.getMessage());
        }
        
        // Fallback: ThreadMXBean 직접 사용
        try {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            return threadBean.getThreadCount();
        } catch (Exception e) {
            log.debug("스레드 수 직접 수집 실패: {}", e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * HTTP 요청 평균 응답 시간 (밀리초)
     */
    private double getAverageResponseTime() {
        try {
            Timer timer = meterRegistry.find("http.server.requests").timer();
            if (timer != null && timer.count() > 0) {
                return timer.mean(TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            log.debug("응답시간 수집 실패: {}", e.getMessage());
        }
        
        return 0.0;
    }
    
    /**
     * HTTP 요청 총 수
     */
    private long getRequestCount() {
        try {
            Timer timer = meterRegistry.find("http.server.requests").timer();
            if (timer != null) {
                return timer.count();
            }
            
            // Counter도 확인
            Counter counter = meterRegistry.find("http.server.requests").counter();
            if (counter != null) {
                return (long) counter.count();
            }
        } catch (Exception e) {
            log.debug("요청 수 수집 실패: {}", e.getMessage());
        }
        
        return 0L;
    }
    
    /**
     * 시스템 부하 평균
     */
    private double getSystemLoadAverage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            return osBean.getSystemLoadAverage();
        } catch (Exception e) {
            log.debug("시스템 부하 수집 실패: {}", e.getMessage());
            return -1.0;
        }
    }
    
    /**
     * 사용 가능한 프로세서 수
     */
    private int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }
    
    /**
     * 최대 메모리 (바이트)
     */
    private long getMaxMemory() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            return memoryBean.getHeapMemoryUsage().getMax();
        } catch (Exception e) {
            return Runtime.getRuntime().maxMemory();
        }
    }
    
    /**
     * 사용 중인 메모리 (바이트)
     */
    private long getUsedMemory() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            return memoryBean.getHeapMemoryUsage().getUsed();
        } catch (Exception e) {
            return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        }
    }
    
    /**
     * 현재 실행 중인 배치 작업 수 (추후 구현)
     */
    private int getActiveBatchJobs() {
        // TODO: 실제 배치 작업 관리자와 연동 필요
        return 0;
    }
    
    /**
     * 배치 작업 대기 큐 크기 (추후 구현)
     */
    private int getBatchQueueSize() {
        // TODO: 실제 큐 관리자와 연동 필요
        return 0;
    }
    
    /**
     * 실패 시 기본값 설정
     */
    private void setDefaultMetrics(Map<String, Object> metrics) {
        metrics.put("instanceId", instanceId);
        metrics.put("timestamp", System.currentTimeMillis());
        metrics.put("cpuUsage", 0.0);
        metrics.put("memoryUsage", 0.0);
        metrics.put("activeThreads", 0);
        metrics.put("responseTime", 0.0);
        metrics.put("requestCount", 0L);
        metrics.put("systemLoadAverage", -1.0);
        metrics.put("availableProcessors", getAvailableProcessors());
        metrics.put("activeJobs", 0);
        metrics.put("queueSize", 0);
        metrics.put("loadScore", 100.0); // 최대 부하로 설정
        metrics.put("isHealthy", false);
        metrics.put("error", "메트릭 수집 실패");
    }
    
    /**
     * 가중평균 부하 점수 계산
     * CPU(35%) + Memory(30%) + Thread(15%) + ResponseTime(10%) + SystemLoad(10%)
     */
    private double calculateLoadScore(Map<String, Object> metrics) {
        double cpuWeight = 0.35;      // CPU 35%
        double memoryWeight = 0.30;   // 메모리 30%
        double threadWeight = 0.15;   // 스레드 15%
        double responseWeight = 0.10; // 응답시간 10%
        double systemWeight = 0.10;   // 시스템 부하 10%
        
        double cpu = (Double) metrics.getOrDefault("cpuUsage", 0.0);
        double memory = (Double) metrics.getOrDefault("memoryUsage", 0.0);
        int threads = (Integer) metrics.getOrDefault("activeThreads", 0);
        double responseTime = (Double) metrics.getOrDefault("responseTime", 0.0);
        double systemLoad = (Double) metrics.getOrDefault("systemLoadAverage", 0.0);
        int processors = (Integer) metrics.getOrDefault("availableProcessors", 4);
        
        // 정규화 (0~100)
        double normalizedCpu = Math.min(Math.max(cpu, 0), 100);
        double normalizedMemory = Math.min(Math.max(memory, 0), 100);
        double normalizedThread = Math.min((threads / (processors * 15.0)) * 100, 100);
        double normalizedResponse = Math.min(Math.max(responseTime / 10.0, 0), 100); // 1초=10점
        double normalizedSystem = systemLoad > 0 ? 
            Math.min((systemLoad / processors) * 100, 100) : 0;
        
        double score = (normalizedCpu * cpuWeight) +
                      (normalizedMemory * memoryWeight) +
                      (normalizedThread * threadWeight) +
                      (normalizedResponse * responseWeight) +
                      (normalizedSystem * systemWeight);
        
        return Math.round(score * 100.0) / 100.0;
    }
    
    /**
     * 인스턴스 건강성 판단
     */
    private boolean isHealthy(Map<String, Object> metrics) {
        double cpu = (Double) metrics.getOrDefault("cpuUsage", 0.0);
        double memory = (Double) metrics.getOrDefault("memoryUsage", 0.0);
        double responseTime = (Double) metrics.getOrDefault("responseTime", 0.0);
        int threads = (Integer) metrics.getOrDefault("activeThreads", 0);
        
        boolean healthy = cpu < 90.0 && 
                         memory < 85.0 && 
                         responseTime < 5000.0 && 
                         threads < 500;
        
        log.debug("건강성 체크 - CPU: {:.1f}%, Memory: {:.1f}%, ResponseTime: {:.1f}ms, Threads: {}, Healthy: {}", 
                cpu, memory, responseTime, threads, healthy);
        
        return healthy;
    }
    
    private String getInstanceId() {
        try {
            String port = System.getProperty("server.port", 
                         System.getenv().getOrDefault("SERVICE_PORT", "20190"));
            String hostname = InetAddress.getLocalHost().getHostName();
            return "service-batch-" + port + "-" + hostname.substring(0, Math.min(hostname.length(), 8));
        } catch (Exception e) {
            String port = System.getProperty("server.port", "20190");
            return "service-batch-" + port + "-" + System.currentTimeMillis() % 10000;
        }
    }
    
    /**
     * 상세 시스템 정보 조회 (디버깅용)
     */
    @GetMapping("/system-info")
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            
            info.put("instanceId", instanceId);
            info.put("jvmVersion", System.getProperty("java.version"));
            info.put("springBootVersion", org.springframework.boot.SpringBootVersion.getVersion());
            info.put("availableProcessors", osBean.getAvailableProcessors());
            info.put("systemLoadAverage", osBean.getSystemLoadAverage());
            
            // JVM 메모리 상세
            info.put("heapMemoryUsed", memoryBean.getHeapMemoryUsage().getUsed());
            info.put("heapMemoryMax", memoryBean.getHeapMemoryUsage().getMax());
            info.put("heapMemoryCommitted", memoryBean.getHeapMemoryUsage().getCommitted());
            info.put("nonHeapMemoryUsed", memoryBean.getNonHeapMemoryUsage().getUsed());
            
            // 스레드 정보
            info.put("threadCount", threadBean.getThreadCount());
            info.put("peakThreadCount", threadBean.getPeakThreadCount());
            info.put("daemonThreadCount", threadBean.getDaemonThreadCount());
            
            // MeterRegistry 정보
            info.put("meterRegistryType", meterRegistry.getClass().getSimpleName());
            info.put("availableMeters", meterRegistry.getMeters().size());
            
        } catch (Exception e) {
            log.error("시스템 정보 수집 실패: {}", e.getMessage(), e);
            info.put("error", e.getMessage());
        }
        
        return info;
    }
}