package org.example.booknuri.domain.user.converter;

import lombok.RequiredArgsConstructor;
import org.example.booknuri.domain.library.dto.LibrarySimpleDTO;
import org.example.booknuri.domain.user.dto.UserInfoResponseDTO;
import org.example.booknuri.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserConverter {

    public UserInfoResponseDTO toDTO(UserEntity user) {
        return UserInfoResponseDTO.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .gender(user.getGender())
                .birth(user.getBirth())
                .myLibrary(user.getMyLibrary() == null ? null : toLibrarySimpleDTO(user))
                .build();
    }

    private LibrarySimpleDTO toLibrarySimpleDTO(UserEntity user) {
        return LibrarySimpleDTO.builder()
                .libCode(user.getMyLibrary().getLibCode())
                .libName(user.getMyLibrary().getLibName())
                .tel(user.getMyLibrary().getTel())
                .homepage(user.getMyLibrary().getHomepage())
                .closed(user.getMyLibrary().getClosed())
                .operatingTime(user.getMyLibrary().getOperatingTime())
                .latitude(user.getMyLibrary().getLatitude())
                .longitude(user.getMyLibrary().getLongitude())
                .bookCount(user.getMyLibrary().getBookCount())
                .fullAddress(user.getMyLibrary().getFullAddress())
                .build();
    }
}
