package com.example.wirebarley.service;

import com.example.wirebarley.domain.User;
import com.example.wirebarley.dto.CreateAccountRequestDTO;
import com.example.wirebarley.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 프레임워크 사용 선언
@DisplayName("UserService 유닛 테스트")
class UserServiceTest {

    @InjectMocks // 테스트 대상 클래스. @Mock으로 만든 객체들이 이 클래스에 주입됩니다.
    private UserService userService;

    @Mock // 의존성을 가짜 객체로 만듭니다.
    private UserRepository userRepository;

    @Test
    @DisplayName("기존 사용자가 존재할 경우, 해당 사용자를 반환하고 save는 호출하지 않는다")
    void findOrCreateUser_WhenUserExists_ShouldReturnExistingUser() {
        // given (준비)
        CreateAccountRequestDTO requestDTO = new CreateAccountRequestDTO("Test User", "test@example.com", "010-1234-5678", "password");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPhone(requestDTO.getPhone());
        existingUser.setUsername(requestDTO.getUsername());

        // userRepository.findByPhone가 호출되면, 'existingUser'를 담은 Optional을 반환하도록 설정
        given(userRepository.findByPhone(requestDTO.getPhone())).willReturn(Optional.of(existingUser));

        // when (실행)
        User resultUser = userService.findOrCreateUser(requestDTO);

        // then (검증)
        // 1. 반환된 사용자가 기존 사용자와 동일한지 확인
        assertThat(resultUser).isEqualTo(existingUser);
        assertThat(resultUser.getId()).isEqualTo(1L);

        // 2. userRepository.findByPhone이 정확히 1번 호출되었는지 확인
        verify(userRepository, times(1)).findByPhone(requestDTO.getPhone());

        // 3. (매우 중요) 사용자가 이미 존재하므로, save 메소드는 절대 호출되면 안 됨을 검증
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("기존 사용자가 존재하지 않을 경우, 새로운 사용자를 생성하고 저장한 후 반환한다")
    void findOrCreateUser_WhenUserDoesNotExist_ShouldCreateAndReturnNewUser() {
        // given (준비)
        CreateAccountRequestDTO requestDTO = new CreateAccountRequestDTO("New User", "new@example.com", "010-9876-5432", "password");

        // userRepository.findByPhone가 호출되면, 비어있는 Optional을 반환하도록 설정 (사용자가 없다는 의미)
        given(userRepository.findByPhone(requestDTO.getPhone())).willReturn(Optional.empty());

        // userRepository.save가 호출될 때의 행동 정의.
        // 여기서는 저장될 새로운 User 객체를 생성하여 반환하도록 설정
        User savedUser = new User();
        savedUser.setId(2L); // DB가 생성해준 ID라고 가정
        savedUser.setPhone(requestDTO.getPhone());
        savedUser.setUsername(requestDTO.getUsername());
        savedUser.setEmail(requestDTO.getEmail());
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        // when (실행)
        User resultUser = userService.findOrCreateUser(requestDTO);

        // then (검증)
        // 1. 반환된 사용자가 null이 아니고, 저장된 사용자와 동일한지 확인
        assertThat(resultUser).isNotNull();
        assertThat(resultUser.getId()).isEqualTo(2L);
        assertThat(resultUser.getUsername()).isEqualTo("New User");

        // 2. userRepository.findByPhone이 정확히 1번 호출되었는지 확인
        verify(userRepository, times(1)).findByPhone(requestDTO.getPhone());

        // 3. (매우 중요) save 메소드에 전달된 User 객체의 내용을 검증하기 위해 ArgumentCaptor 사용
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        // 4. 캡처된 객체의 필드들이 DTO의 값으로 잘 설정되었는지 확인
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getId()).isNull(); // save 되기 전이므로 ID는 null이어야 함
        assertThat(capturedUser.getPhone()).isEqualTo(requestDTO.getPhone());
        assertThat(capturedUser.getEmail()).isEqualTo(requestDTO.getEmail());
        assertThat(capturedUser.getUsername()).isEqualTo(requestDTO.getUsername());
    }
}