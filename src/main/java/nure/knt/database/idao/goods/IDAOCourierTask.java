package nure.knt.database.idao.goods;

import nure.knt.database.idao.core.IDAOCoreSave;
import nure.knt.entity.enums.ConditionCommodity;
import nure.knt.entity.enums.Role;
import nure.knt.entity.goods.CourierTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface IDAOCourierTask<Task extends CourierTask> extends IDAOCoreSave<Task> {

    public List<Task> findByRoleAndIdUserAndCityContaining(Role role,Long id,String city );
    public List<Task> findByRoleAndIdUserAndEmailContaining(Role role,Long id,String email);
    public List<Task> findByRoleAndIdUserAndNameCourierContaining(Role role,Long id,String city);
    public List<Task> findByRoleAndIdUserAndNameAdminContaining(Role role,Long id,String city);
    public List<Task> findByRoleAndIdUserAndDescribeContaining(Role role,Long id,String city);
    public List<Task> findByRoleAndIdUserAndDateRegistrationBetween(Role role, Long id, LocalDateTime startDate,LocalDateTime endDate);
    public List<Task> findByRoleAndIdUserAndNumberOfFlyersBetween(Role role, Long id, int start,int end);
    public List<Task> findByRoleAndIdUserAndConditionCommodityIn(Role role, Long id, Set<ConditionCommodity> conditionCommodities);
}