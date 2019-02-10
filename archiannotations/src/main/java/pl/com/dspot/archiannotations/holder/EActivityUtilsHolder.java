package pl.com.dspot.archiannotations.holder;

import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.IJStatement;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JFormatter;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;
import org.androidannotations.holder.EActivityHolder;
import org.androidannotations.plugin.PluginClassHolder;
import pl.com.dspot.archiannotations.helper.override.APTCodeModelHelper;

import java.io.StringWriter;

public class EActivityUtilsHolder extends PluginClassHolder<EActivityHolder> {

    private JBlock onCreateAfterSuperBlock;
    private JBlock onCreateAfterSuperInjectionsBlock;

    private APTCodeModelHelper codeModelHelper;

    public EActivityUtilsHolder(EActivityHolder holder) {
        super(holder);
        this.codeModelHelper = new APTCodeModelHelper(environment());
    }

    public JBlock getOnCreateAfterSuperBlock() {
        if (onCreateAfterSuperBlock == null) {
            setBlockAfterSuperCall();
        }
        return onCreateAfterSuperBlock;
    }

    public JBlock getOnCreateAfterSuperInjectionsBlock() {
        if (onCreateAfterSuperInjectionsBlock == null) {
            setBlockAfterSuperCall();
        }
        return onCreateAfterSuperInjectionsBlock;
    }

    private void setBlockAfterSuperCall() {

        JMethod onCreateMethod = holder().getOnCreate();
        JBlock previousBody = codeModelHelper.removeBody(onCreateMethod);
        JBlock newBody = onCreateMethod.body();
        JBlock blockAfterSuper = new JBlock();

        //TODO Replace calls to super, if any
        for (Object content : previousBody.getContents()) {

            if (content instanceof IJStatement) {

                StringWriter writer = new StringWriter();
                JFormatter formatter = new JFormatter(writer);
                IJStatement statement = (IJStatement) content;
                statement.state(formatter);
                String statementString = writer.getBuffer().toString();

                if (statementString.trim().startsWith("super.")) {
                    newBody.add((IJStatement) content);
                    newBody.add(blockAfterSuper);
                    continue;
                }

            }

            if (content instanceof JVar) {
                JVar var = (JVar) content;
                try {
                    java.lang.reflect.Field varInitField = JVar.class.getDeclaredField("m_aInitExpr");
                    varInitField.setAccessible(true);
                    IJExpression varInit = (IJExpression) varInitField.get(var);

                    newBody.decl(var.type(), var.name(), varInit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                newBody.add((IJStatement) content);
            }

        }

        onCreateAfterSuperBlock = blockAfterSuper.blockVirtual();
        onCreateAfterSuperInjectionsBlock = blockAfterSuper.blockVirtual();

    }

}
