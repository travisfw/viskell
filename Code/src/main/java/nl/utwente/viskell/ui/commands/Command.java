package nl.utwente.viskell.ui.commands;

import nl.utwente.viskell.ui.CustomUIPane;

public abstract class Command {
    CustomUIPane pane;

    public Command(CustomUIPane pane) {
        this.pane = pane;
    }

    public abstract void redo();
    public abstract void undo();

    public abstract String toString();
}