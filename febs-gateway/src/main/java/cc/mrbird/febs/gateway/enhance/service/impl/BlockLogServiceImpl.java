package cc.mrbird.febs.gateway.enhance.service.impl;

import cc.mrbird.febs.common.entity.QueryRequest;
import cc.mrbird.febs.common.utils.DateUtil;
import cc.mrbird.febs.gateway.enhance.entity.BlockLog;
import cc.mrbird.febs.gateway.enhance.mapper.BlockLogMapper;
import cc.mrbird.febs.gateway.enhance.service.BlockLogService;
import cc.mrbird.febs.gateway.enhance.utils.AddressUtil;
import cc.mrbird.febs.gateway.enhance.utils.PageableExecutionUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * @author MrBird
 */
@Service
public class BlockLogServiceImpl implements BlockLogService {

    @Autowired(required = false)
    private BlockLogMapper blockLogMapper;
    @Autowired(required = false)
    private ReactiveMongoTemplate template;

    @Override
    public Mono<BlockLog> create(BlockLog blockLog) {
        blockLog.setCreateTime(DateUtil.formatFullTime(LocalDateTime.now(), DateUtil.FULL_TIME_SPLIT_PATTERN));
        blockLog.setLocation(AddressUtil.getCityInfo(blockLog.getIp()));
        return blockLogMapper.insert(blockLog);
    }

    @Override
    public Flux<BlockLog> delete(String ids) {
        String[] idArray = StringUtils.splitByWholeSeparatorPreserveAllTokens(ids, ",");
        return blockLogMapper.deleteByIdIn(idArray);
    }

    @Override
    public Flux<BlockLog> findPages(QueryRequest request, BlockLog blockLog) {
        Query query = getQuery(blockLog);
        return PageableExecutionUtil.getPages(query, request, BlockLog.class, template);
    }

    @Override
    public Mono<Long> findCount(BlockLog blockLog) {
        Query query = getQuery(blockLog);
        return template.count(query, BlockLog.class);
    }

    private Query getQuery(BlockLog blockLog) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        if (StringUtils.isNotBlank(blockLog.getRequestMethod())) {
            criteria.and("requestMethod").is(blockLog.getRequestMethod());
        }
        if (StringUtils.isNotBlank(blockLog.getIp())) {
            criteria.and("ip").is(blockLog.getIp());
        }
        if (StringUtils.isNotBlank(blockLog.getCreateTimeFrom())
                && StringUtils.isNotBlank(blockLog.getCreateTimeTo())) {
            criteria.andOperator(
                    Criteria.where("createTime").gt(blockLog.getCreateTimeFrom()),
                    Criteria.where("createTime").lt(blockLog.getCreateTimeTo())
            );
        }
        if (StringUtils.isNotBlank(blockLog.getRequestUri())) {
            criteria.and("requestUri").is(blockLog.getRequestUri());
        }
        query.addCriteria(criteria);
        return query;
    }
}
