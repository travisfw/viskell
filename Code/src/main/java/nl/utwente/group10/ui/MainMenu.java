package nl.utwente.group10.ui;

import java.io.IOException;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import nl.utwente.group10.haskell.catalog.Entry;
import nl.utwente.group10.haskell.catalog.HaskellCatalog;
import nl.utwente.group10.ui.components.blocks.Block;
import nl.utwente.group10.ui.components.blocks.ValueBlock;
import nl.utwente.group10.ui.components.blocks.DisplayBlock;
import nl.utwente.group10.ui.components.blocks.FunctionBlock;

public class MainMenu extends ContextMenu {
    private CustomUIPane parent;

    public MainMenu(HaskellCatalog catalog, CustomUIPane tactilePane) {
        parent = tactilePane;

        for (String category : catalog.getCategories()) {
            Menu submenu = new Menu(category);

            for (Entry entry : catalog.getCategory(category)) {
                MenuItem item = new MenuItem(entry.getName());
                item.setOnAction(event -> addFunctionBlock(entry));
                submenu.getItems().add(item);
        }

            this.getItems().addAll(submenu);
        }

        MenuItem valueBlockItem = new MenuItem("Value Block");
        valueBlockItem.setOnAction(event -> addValueBlock());
        MenuItem displayBlockItem = new MenuItem("Display Block");
        displayBlockItem.setOnAction(event -> addDisplayBlock());
        
        //TODO remove this item when debugging of visualFeedback is done
        MenuItem errorItem = new MenuItem("Error all Blocks");
        errorItem.setOnAction(event -> parent.errorAll());

        MenuItem quitItem = new MenuItem("Quit");
        quitItem.setOnAction(event -> System.exit(0));

        SeparatorMenuItem sep = new SeparatorMenuItem();

        this.getItems().addAll(valueBlockItem, displayBlockItem, sep, errorItem, quitItem);
    }

    private void addFunctionBlock(Entry entry) {
        try {
            FunctionBlock fb = new FunctionBlock(entry.getName(), entry.getType(), parent);
            addBlock(fb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addValueBlock() {
        try {
            addBlock(new ValueBlock(parent));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addDisplayBlock() {
        try {
            addBlock(new DisplayBlock(parent));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addBlock(Block block) {
        parent.getChildren().add(block);
    }

}
