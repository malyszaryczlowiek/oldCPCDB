package com.github.malyszaryczlowiek.cpcdb.Controllers;

import net.bytebuddy.build.ToStringPlugin;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;

@DisplayName("Tests for MainStageController class")
class MainStageControllerTest
{

    @Nested
    @DisplayName("Tests for searchingCriteriaChosen() method")
    class SearchingCriteriaChosenMethod
    {
        @Test
        @DisplayName("Does filter remove all unnecessary spaces from smiles criteria")
        void doesFilterRemoveAllUnnecessarySpacesFromSmilesCriteria()
        {
            String smilesFilter = "abcd";
            String smiles = "   abcd   ";
            String smilesSearchCriteria = smiles.replaceAll("[ ]+", "");

            Assertions.assertThat(smilesFilter).isEqualTo(smilesSearchCriteria);
        }

        @Test
        @DisplayName("Does filter remove all unnecessary characters from Form criteria")
        void doesFilterRemoveAllUnnecessaryCharactersFromFormCriteria()
        {
            String form = "   mam, tak ; samo :Jak .. Osad;; bialy;krystaliczny     ";

            String formWithoutSpaces = form.trim()
                    .replaceAll("[,;:.]+"," ")
                    .replaceAll("[ ]{2,}", " ")
                    .toLowerCase();

            String correctResult = "mam tak samo jak osad bialy krystaliczny";

            Assertions.assertThat(formWithoutSpaces).isEqualTo(correctResult);
        }

        @Test
        @DisplayName("Does form filter Any Matching Word In Form Field")
        void doesFormFilterFindAnyMatchingWordInFormField()
        {
            String formSearchingWords = "   mam, tak ; samo :Jak .. Osad;; bialy;krystaliczny     ";

            String formSearchingWordsWithoutSpaces = formSearchingWords.trim()
                    .replaceAll("[,;:.]+"," ")
                    .replaceAll("[ ]{2,}", " ")
                    .trim()
                    .toLowerCase();

            String formFromCompoundLowercase = "     lsdhgjklsa   ,.,.,h lasjdhg Osad::,.,::jkasgh    ksjafgh ,.,klasfh   "
                    .trim()
                    .replaceAll("[,;:.]+"," ")
                    .replaceAll("[ ]{2,}", " ")
                    .trim()
                    .toLowerCase();

            boolean resultTrue = Arrays.stream(formFromCompoundLowercase.split(" "))
                    .anyMatch(wordFromCompoundForm ->  formSearchingWordsWithoutSpaces.contains(wordFromCompoundForm)
                    );

            String formFromCompoundLowercase2 = "     lsdhgjklsa   ,.,.,h lasjdhg Oosad::,.,::jkasgh    ksjafgh ,.,klasfh   "
                    .trim()
                    .replaceAll("[,;:.]+"," ")
                    .replaceAll("[ ]{2,}", " ")
                    .trim()
                    .toLowerCase();

            boolean resultFalse = Arrays.stream(formFromCompoundLowercase2.split(" "))
                    .anyMatch(wordFromCompoundForm ->  formSearchingWordsWithoutSpaces.contains(wordFromCompoundForm)
                    );


            org.junit.jupiter.api.Assertions.assertAll(
                    () -> Assertions.assertThat(resultTrue).isTrue(),
                    () -> Assertions.assertThat(resultFalse).isFalse()
            );
        }

        @Test
        @DisplayName("Does trim() remove single space")
        void doesTrimRemoveSingleSpace()
        {
            Assertions.assertThat(" ".trim()).isEqualTo("");
        }

        @Test
        @DisplayName("testring of date validity")
        void testDate()
        {
            LocalDate now = LocalDate.now();
            String nowS = now.toString();
            LocalDate tomorrow = LocalDate.parse(nowS).plusDays(1);
            Assertions.assertThat(now).isBefore(tomorrow);
        }
    }
}

































