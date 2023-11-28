package io.quarkus.it.artemis.ra.profile;

public class DisableAllServices extends DisableDataBaseService {
    @Override
    public boolean disableGlobalTestResources() {
        return true;
    }
}
