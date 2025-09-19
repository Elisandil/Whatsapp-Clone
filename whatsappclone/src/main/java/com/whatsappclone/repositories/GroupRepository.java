package com.whatsappclone.repositories;

import com.whatsappclone.entities.group.Group;
import com.whatsappclone.entities.group.GroupConstants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, String> {

    @Query(name = GroupConstants.FIND_GROUPS_BY_USER_ID)
    List<Group> findGroupsByUserId(@Param("userId") String userId);
}
