package com.universal.scheduler.job;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GenerateCodePoolJob implements Job {

    private final static Logger logger = LoggerFactory.getLogger(GenerateCodePoolJob.class);

//    @Autowired
//    private CustomerMapper customerMapper;

//    @Autowired
//    private RedisTemplate<String, String> globalRedisTemplate;
//
//    private int MAX_CRM_QUANTITY = 1000;

    @Scheduled(cron = "0 0 3 * * ?")
    @Override
    public void run() {

        logger.info(StringUtils.center("Start " + this.getClass().getName(), 60, '-'));

        generateCrmCodePool();

        logger.info(StringUtils.center("End " + this.getClass().getName(), 60, '-'));
    }

    private void generateCrmCodePool() {

//        CustomerBean customer = customerMapper.findMaxCode();
//
//        String maxCode = customer.getCode();
//        logger.info("Max CRM code is {}.", maxCode);
//
//        ListOperations<String, String> listOperations = globalRedisTemplate.opsForList();
//
//        long poolSize = listOperations.size(GlobalCacheKey.CRM_CODE_POOL);
//
//        for (int i = 0; i < poolSize; i++) {
//
//            String code0 = listOperations.leftPop(GlobalCacheKey.CRM_CODE_POOL);
//
//            if (maxCode.compareTo(code0) < 0) {
//                listOperations.leftPush(GlobalCacheKey.CRM_CODE_POOL, code0);
//                break;
//            } else {
//                listOperations.remove(GlobalCacheKey.CRM_CODE_POOL, 0, code0);
//            }
//        }
//
//        poolSize = listOperations.size(GlobalCacheKey.CRM_CODE_POOL);
//
//        if (poolSize >= 1) {
//            maxCode = listOperations.index(GlobalCacheKey.CRM_CODE_POOL, poolSize - 1);
//        }
//
//        String[] codes = new String[MAX_CRM_QUANTITY - (int) poolSize];
//        logger.info("{} CRM codes will be generated.", codes.length);
//
//        if (codes.length > 0) {
//
//            for (int i = 0; i < codes.length; i++) {
//
//                codes[i] = CodeUtils.generateCustomerCode(i == 0 ? maxCode : codes[i - 1]);
//            }
//
//            listOperations.rightPushAll(GlobalCacheKey.CRM_CODE_POOL, codes);
//        }
    }

    @Override
    public void run(String paramters) {

    }
}
