package io.ula.drng.dialog;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.dialog.body.DialogBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DialogBuilder {
    private Component title;
    private DialogAction afterAction = DialogAction.CLOSE;
    private List<Input> inputs = new ArrayList<>();
    private List<DialogBody> bodys = new ArrayList<>();
    private List<ActionButton> buttons = new ArrayList<>();
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
    public DialogBuilder bodys(List<DialogBody> bodys){
        this.bodys = bodys;
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
    public Dialog build(){
        CommonDialogData commonDialogData = new CommonDialogData(
                this.title,
                Optional.empty(),
                false,
                true,
                afterAction,
                bodys,
                inputs
        );
        for(ActionButton button : buttons){
            button.action().ifPresent(action -> {
                if(action instanceof CustomAll customAll)
                    DialogHelper.addCustomClick(customAll.id(),customAll);
            });
        }
        return new MultiActionDialog(commonDialogData,buttons, Optional.empty(),inputs.size()+bodys.size());
    }
}
