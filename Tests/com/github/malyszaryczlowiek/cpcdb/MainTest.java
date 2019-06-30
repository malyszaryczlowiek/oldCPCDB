package com.github.malyszaryczlowiek.cpcdb;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@DisplayName("Tests for Main class")
class MainTest
{

    @Test
    @DisplayName("Parent object is not null")
    void isParentObjectNotNull()
    {
        try
        {
            Parent root  = FXMLLoader.load(getClass().getResource("../../../../../main/res/mainStage.fxml"));
            Assertions.assertThat(root).isNotNull();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}