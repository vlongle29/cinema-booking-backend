package com.example.CineBook.repository.irepository;

import com.example.CineBook.dto.room.RoomSearchDTO;
import com.example.CineBook.model.Room;
import com.example.CineBook.repository.base.BaseRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID>, BaseRepositoryCustom<Room, RoomSearchDTO> {
    List<Room> findByBranchId(UUID branchId);
    long countByBranchId(UUID branchId);
}
