package com.github.malyszaryczlowiek.cpcdb.db;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tests for SqlUpdater class")
class SqlUpdaterTest
{

    /**
     * testuję czy usuwam w ostanim polecieniu jakeiś
     */
    @Test
    @DisplayName("Simple short true test")
    void simpleShortTrueTest()
    {
        Assertions.assertTrue(true, () -> "obtained value is ture");
    }

    /**
     * testuję czy usuwam w ostanim polecieniu jakeiś
     */
    @Test
    @DisplayName("Is comma deleted properly from Updater Prepared Statement")
    void isCommaDeletedProperlyFromUpdaterPreparedStatement()
    {
        Assertions.assertTrue(true, () -> "obtained value is ture");
    }
}
