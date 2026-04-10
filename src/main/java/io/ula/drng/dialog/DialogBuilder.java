package io.ula.drng.dialog;

import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.dialog.body.DialogBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DialogBuilder {
    private Component title;
    private DialogAction afterAction = DialogAction.CLOSE;
    private List<Input> inputs = new ArrayList<>();
    private List<DialogBody> bodies = new ArrayList<>();
    private List<ActionButton> buttons = new ArrayList<>();
    private boolean cancellable = false;
    public DialogBuilder(Component title){
        this.title = title;
    }
    public DialogBuilder(){
        this.title = Component.literal("Title");
    }
    public DialogBuilder setName(Component component){
        this.title = title;
        return this;
    }
    public DialogBuilder inputs(List<Input> inputs){
        this.inputs = inputs;
        return this;
    }
    public DialogBuilder bodies(List<DialogBody> bodies){
        this.bodies = bodies;
        return this;
    }
    public DialogBuilder actions(List<ActionButton> buttons){
        this.buttons = buttons;
        return this;
    }
    public DialogBuilder afterAction(DialogAction action){
        this.afterAction = action;
        return this;
    }
    public DialogBuilder cancellable(Boolean cancellable){
        this.cancellable = cancellable;
        return this;
    }
    public Dialog build(){
        CommonDialogData commonDialogData = new CommonDialogData(
                this.title,
                Optional.empty(),
                this.cancellable,
                true,
                afterAction,
                bodies,
                inputs
        );
        for(ActionButton button : buttons){
            button.action().ifPresent(action -> {
                if(action instanceof CustomAll customAll)
                    DialogHelper.addCustomClick(customAll.id(),customAll);
            });
        }
        return new MultiActionDialog(commonDialogData,buttons, Optional.empty(),inputs.size()+bodies.size());
    }
}
