package test.mysql;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowlong.bpm.engine.entity.FlwProcess;
import com.flowlong.bpm.mybatisplus.mapper.FlwProcessMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * issue 问题测试
 */
public class TestIssue extends MysqlTest {

    @Autowired
    protected FlwProcessMapper flwProcessMapper;

    /**
     * 测试流程分页查询
     */
    @Test
    public void testFlwProcessPage() {
        LambdaQueryWrapper<FlwProcess> lqw = Wrappers.<FlwProcess>lambdaQuery()
                .like(FlwProcess::getProcessName, "审批")
                .orderByDesc(FlwProcess::getSort);
        Page<FlwProcess> page = flwProcessMapper.selectPage(Page.of(1, 10), lqw);
        Assertions.assertTrue(page.getPages() >= 0);
    }
}
