package com.github.malyszaryczlowiek.cpcdb.Additions;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class SearchingCriteriaTest
{

    @Test
    @DisplayName("Test cutting whitespaces and spliting string with semicolons")
    void hasStringRemovedWhitespacesAndIsSplitedWithSemicolon()
    {
        String testString = " mieszam jakieś    bagno ; i nie wiem ; nak mam to zrobić";
        String[] result = testString.replaceAll(" ","").split(";");
        Assertions.assertThat(result).isEqualTo(new String[]{"mieszamjakieśbagno","iniewiem","nakmamtozrobić"});
    }

}