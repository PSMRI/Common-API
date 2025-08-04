package com.iemr.common.service.everwellStoreRecords;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoreRecordServiceImplTest {
    @Test
    void canInstantiate() {
        StoreRecordServiceImpl service = new StoreRecordServiceImpl();
        assertNotNull(service);
    }
}
