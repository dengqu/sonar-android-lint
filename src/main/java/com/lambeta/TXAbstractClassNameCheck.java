package com.lambeta;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

/**
 * @author 孙泽明
 * 抽象类命名使用TXAbstract或TXBase开头
 */
@Rule(
        // 规则id
        key = "TXAbstractClassNameCheck",
        // 规则名称
        name = "抽象类命名使用TXAbstract或TXBase开头",
        // 规则介绍
        description = "抽象类命名使用TXAbstract或TXBase开头",
        // 规则标签
        tags = {"wxtx-java"},
        // 规则级别
        priority = Priority.MINOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.ARCHITECTURE_CHANGEABILITY)
// 纠正错误所需时间
@SqaleConstantRemediation("10min")
public class TXAbstractClassNameCheck extends BaseTreeVisitor implements JavaFileScanner {
    private JavaFileScannerContext context;
    public void scanFile(JavaFileScannerContext context) {
        this.context = context;
        scan(context.getTree());
    }

    @Override
    public void visitClass(ClassTree tree) {
        if(tree == null || tree.simpleName() == null){
            super.visitClass(tree);
            return;
        }

        String className = tree.simpleName().name();
        boolean isAbstract = tree.symbol().isAbstract();
        if(isAbstract && isNameIll(className)){
            context.reportIssue(this, tree, "抽象类命名使用TXAbstract或TXBase开头");
        }
        super.visitClass(tree);
    }

    private boolean isNameIll(String className){
        return !className.startsWith("TXAbstract") && !className.startsWith("TXBase");
    }
}
