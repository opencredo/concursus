package com.opencredo.concourse.spring.commands.processing;

import com.opencredo.concourse.domain.commands.CommandType;
import com.opencredo.concourse.domain.commands.CommandTypeInfo;
import com.opencredo.concourse.domain.commands.CommandTypeMatcher;
import com.opencredo.concourse.mapping.commands.methods.reflection.CommandInterfaceInfo;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class ComponentScanningCommandTypeMatcher implements CommandTypeMatcher, ApplicationContextAware {

    private final Map<CommandType, CommandTypeInfo> commandTypeMap = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationContext.getBeansWithAnnotation(CommandHandler.class).values().forEach(this::addTypeInfo);
    }

    private void addTypeInfo(Object handler) {
        Class<?> handlerInterface = CommandProcessorReflection.getHandlerInterface(handler);
        commandTypeMap.putAll(CommandInterfaceInfo.forInterface(handlerInterface).getTypeInfoMap());
    }

    @Override
    public Optional<CommandTypeInfo> match(CommandType commandType) {
        return Optional.ofNullable(commandTypeMap.get(commandType));
    }
}
