package com.tts.techtalenttwitter.repositories;

import com.tts.techtalenttwitter.models.Role;
import com.tts.techtalenttwitter.models.Role.RoleType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Wes Lanning
 * @version 2019-11-25
 */
@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {
    Role findByRole(RoleType role);
}
