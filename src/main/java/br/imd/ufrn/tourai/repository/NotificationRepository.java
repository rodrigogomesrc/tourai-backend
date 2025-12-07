package br.imd.ufrn.tourai.repository;

import br.imd.ufrn.tourai.model.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.destination.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findRecent(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.destination.id = :userId AND n.received = false ORDER BY n.createdAt DESC")
    List<Notification> findUnread(@Param("userId") Integer userId);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.received = true WHERE n.id = :id")
    void markAsReceived(@Param("id") Integer id);

}
