package com.nomad.pm.test.pm;

import java.util.Collection;

import org.junit.Ignore;
import org.slf4j.LoggerFactory;

import com.nomad.exception.ModelNotExistException;
import com.nomad.model.Criteria;
import com.nomad.pm.PmDataInvokerFactory;
import com.nomad.server.PmDataInvoker;
import com.nomad.pm.test.criteria.ChildCriteria;
import com.nomad.pm.test.criteria.TestCriteria;
import com.nomad.pm.test.models.ChildId;
import com.nomad.pm.test.models.MainClass;
import com.nomad.pm.test.models.TestId;
@Ignore
public class LoadData {
    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LoadData.class);

    /**
     * @param args
     */
    public static void main(final String[] args) {

        final LoadData ld = new LoadData();
        try {
            ld.load();
        } catch (final ModelNotExistException e) {
            LOGGER.error(e.getMessage(),e);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
    }

    private void load() throws Exception{
        final PmDataInvoker dataInvoker = PmDataInvokerFactory.getDataInvoker("b", "org.postgresql.Driver",
                "jdbc:postgresql://localhost:5432/test", "test", "test","pm.cfg.xml",1);


        final MainClass model1=(MainClass) dataInvoker.getModel(new TestId(1));
        final MainClass model2=(MainClass) dataInvoker.getModel(new TestId(2));
        LOGGER.debug("Debug!");
        LOGGER.info("model1:{} model2:{}",model1,model2);

        final TestCriteria tc= new TestCriteria();
        Collection<MainClass> result = dataInvoker.getList(tc).getResultList();
        LOGGER.info("list1:{}",result);

        tc.addCriterion(TestCriteria.ID,Criteria.Condition.LT, 3);
        tc.addRelationLoad("rChild");
        result = dataInvoker.getList(tc).getResultList();
        LOGGER.info("list2:{}",result);

        // test child
        LOGGER.info("chmodel1:{} chmodel2:{}", dataInvoker.getModel(new ChildId(1)) , dataInvoker.getModel(new ChildId(2)));

        final ChildCriteria chc= new ChildCriteria();

        LOGGER.info("listchild1:{}", dataInvoker.getList(chc));

        chc.addCriterion(ChildCriteria.ID, Criteria.Condition.LT, 3);

        LOGGER.info("listchild1:{}", dataInvoker.getList(chc));


        dataInvoker.close();

    }
}
