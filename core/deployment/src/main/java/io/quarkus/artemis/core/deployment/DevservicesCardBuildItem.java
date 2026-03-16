package io.quarkus.artemis.core.deployment;

import java.util.Collections;
import java.util.List;

import io.quarkus.builder.item.SimpleBuildItem;
import io.quarkus.devui.spi.page.PageBuilder;

public final class DevservicesCardBuildItem extends SimpleBuildItem {
    private final List<PageBuilder<?>> pagesToAdd;

    public DevservicesCardBuildItem(List<PageBuilder<?>> pagesToAdd) {
        this.pagesToAdd = Collections.unmodifiableList(pagesToAdd);
    }

    public List<PageBuilder<?>> getPagesToAdd() {
        return pagesToAdd;
    }
}
