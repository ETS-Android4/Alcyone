package com.pleiades.pleione.alcyone;

import java.util.ArrayList;

public class ConversationControl {
    public static boolean inConversation = false;
    public static boolean inConversationInstant = false;
    public static boolean inTalking = false;
    public static boolean inTalkingInstant = false;
    public static boolean inTutorial = false;
    public static boolean conversationLock = false;
    public static int conversationCount;
    public static ArrayList<ConversationBlock> conversation;
    public static ArrayList<ConversationBlock> conversationInstant;
    public static ConversationBlock conversationBlock;

    public static void addConversation(ConversationScript... scripts) {
        if ((!LayoutAlcyone.isAlcyoneSleep) && (LayoutAlcyone.storyRequest == -1) && !ConversationControl.inConversation && !ConversationControl.inConversationInstant) {
            conversationBlock = new ConversationBlock();

            for (ConversationScript script : scripts) {
                if (script != null)
                    conversationBlock.block.add(script);
            }
            conversation.add(conversationBlock);

            // fab count
            conversationCount = conversation.size();
            MainActivity.fabCountControl();

            // do not save now
            //PrefsController.setConversationPrefs(context, "conversation", conversation);
        }
    }

    public static void addConversationInstant(ConversationScript... scripts) {
        conversationBlock = new ConversationBlock();

        for (ConversationScript script : scripts) {
            if (script != null)
                conversationBlock.block.add(script);
        }
        conversationInstant.add(conversationBlock);
    }
}

// ArrayList<conversationBlock> conversation is finally saved
class ConversationBlock {
    ArrayList<ConversationScript> block = new ArrayList<>();
}

class ConversationScript {
    String script;
    String facialExpression;
    boolean isAlcyone;
    boolean noNamed;
    boolean visibilityAlcyone; // character image visibility
    boolean visibilityPleione;
    boolean sceneChange;
    boolean vibrate = false;

    // constructor
    public ConversationScript(String script, String facialExpression, boolean isAlcyone, boolean visibilityAlcyone, boolean visibilityPleione) {
        this.script = script;
        this.facialExpression = facialExpression;
        this.isAlcyone = isAlcyone;
        this.visibilityAlcyone = visibilityAlcyone;
        this.visibilityPleione = visibilityPleione;
        this.sceneChange = false;
        this.noNamed = false;
    }

    public ConversationScript(String script, String facialExpression, boolean isAlcyone) {
        this.script = script;
        this.facialExpression = facialExpression;
        this.isAlcyone = isAlcyone;
        this.visibilityAlcyone = true;
        this.visibilityPleione = true;
        this.sceneChange = false;
        this.noNamed = false;
    }

    public ConversationScript(String script, String facialExpression) {
        this.script = script;
        this.facialExpression = facialExpression;
        this.isAlcyone = true;
        this.visibilityAlcyone = true;
        this.visibilityPleione = true;
        this.sceneChange = false;
        this.noNamed = false;
    }

    public ConversationScript(String script, boolean noNamed, boolean visibilityAlcyone, boolean visibilityPleione) {
        this.script = script;
        this.facialExpression = null;
        this.noNamed = noNamed;
        this.isAlcyone = false;
        this.sceneChange = false;
        this.visibilityAlcyone = visibilityAlcyone;
        this.visibilityPleione = visibilityPleione;
    }

    public ConversationScript(String script, boolean noNamed, boolean vibrate) {
        this.script = script;
        this.facialExpression = null;
        this.noNamed = noNamed;
        this.isAlcyone = false;
        this.sceneChange = false;
        this.vibrate = vibrate;
    }

    public ConversationScript(String script, boolean noNamed) {
        this.script = script;
        this.facialExpression = null;
        this.noNamed = noNamed;
        this.isAlcyone = false;
        this.sceneChange = false;
        this.visibilityAlcyone = true;
        this.visibilityPleione = true;
    }

    public ConversationScript(boolean sceneChange) {
        this.sceneChange = sceneChange;
    }
}