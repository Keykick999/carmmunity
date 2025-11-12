package com.example.carmmunity_capstone.service; // 경로는 알맞게 수정하세요

import com.example.carmmunity_capstone.dto.ChatMessageDto;
import com.example.carmmunity_capstone.dto.ChatRoomListDto;
import com.example.carmmunity_capstone.dto.CreateChatRoomRequest;
import com.example.carmmunity_capstone.dto.CreateChatRoomResponse;
import com.example.carmmunity_capstone.entity.*;
import com.example.carmmunity_capstone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository; // User 조회를 위해 필요
    private final ChatMessageRepository chatMessageRepository;
    private final MessageReadStatusRepository messageReadStatusRepository;

    /**
     * 1:1 또는 그룹 채팅방 생성
     */
    public CreateChatRoomResponse createChatRoom(String currentUserUuid, CreateChatRoomRequest request) {

        // 1. 현재 사용자 조회
        User currentUser = findUserByUuid(currentUserUuid);

        List<String> targetUuids = request.getTargetUserUuids();

        // 2. 요청 타입 구분 (1:1 vs 그룹)
        if (targetUuids == null || targetUuids.isEmpty()) {
            throw new RuntimeException("대화 상대를 지정해야 합니다.");
        }

        if (targetUuids.size() == 1) {
            // 1:1 채팅방 생성 로직
            User targetUser = findUserByUuid(targetUuids.get(0));
            return createOneToOneRoom(currentUser, targetUser);
        } else {
            // 그룹 채팅방 생성 로직
            List<User> targetUsers = targetUuids.stream().map(this::findUserByUuid).toList();
            return createGroupRoom(currentUser, targetUsers, request.getRoomName());
        }
    }

    /**
     * 1:1 채팅방 생성 (또는 기존 방 조회)
     */
    private CreateChatRoomResponse createOneToOneRoom(User userA, User userB) {
        // 1. ID 정렬 (user1이 항상 ID가 작은 쪽)
        User user1 = (userA.getUserUuid().compareTo(userB.getUserUuid()) < 0) ? userA : userB;
        User user2 = (userA.getUserUuid().compareTo(userB.getUserUuid()) < 0) ? userB : userA;

        // 2. 기존 1:1 방이 있는지 DB에서 조회 (핵심 로직)
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByUser1AndUser2AndType(
                user1, user2, ChatRoomType.ONE_TO_ONE
        );

        if (existingRoom.isPresent()) {
            // 3-1. 방이 이미 존재하면, 기존 방 ID 반환
            ChatRoom room = existingRoom.get();
            // 방 이름은 상대방 이름으로 설정
            String roomName = userA.getUserUuid().equals(user1.getUserUuid()) ? user2.getNickname() : user1.getNickname();
            return new CreateChatRoomResponse(room.getChatRoomId(), roomName);
        }

        // 3-2. 방이 없으면 새로 생성
        ChatRoom newRoom = ChatRoom.builder()
                .type(ChatRoomType.ONE_TO_ONE)
                .user1(user1)
                .user2(user2)
                .build();
        chatRoomRepository.save(newRoom);

        // 4. 참여자 정보(ChatParticipant) 생성 (A, B)
        ChatParticipant participantA = ChatParticipant.builder().user(userA).chatRoom(newRoom).build();
        ChatParticipant participantB = ChatParticipant.builder().user(userB).chatRoom(newRoom).build();
        chatParticipantRepository.saveAll(List.of(participantA, participantB));

        // 방 이름은 상대방 이름으로 설정
        String roomName = userA.getUserUuid().equals(user1.getUserUuid()) ? user2.getNickname() : user1.getNickname();
        return new CreateChatRoomResponse(newRoom.getChatRoomId(), roomName);
    }

    /**
     * 그룹 채팅방 생성
     */
    private CreateChatRoomResponse createGroupRoom(User creator, List<User> targetUsers, String roomName) {
        // 1. 그룹 채팅방 생성 (이름 설정)
        ChatRoom newRoom = ChatRoom.builder()
                .type(ChatRoomType.GROUP)
                .roomName(roomName != null ? roomName : generateDefaultGroupName(creator, targetUsers))
                .build();
        chatRoomRepository.save(newRoom);

        // 2. 참여자 정보(ChatParticipant) 생성 (creator + targetUsers)
        List<ChatParticipant> participants = new ArrayList<>();
        participants.add(ChatParticipant.builder().user(creator).chatRoom(newRoom).build()); // 나

        for (User targetUser : targetUsers) {
            participants.add(ChatParticipant.builder().user(targetUser).chatRoom(newRoom).build()); // 상대방들
        }
        chatParticipantRepository.saveAll(participants);

        return new CreateChatRoomResponse(newRoom.getChatRoomId(), newRoom.getRoomName());
    }

    /**
     * 2. 채팅 목록 조회 (API ①)
     */
    @Transactional(readOnly = true)
    public List<ChatRoomListDto> getChatRoomList(String currentUserUuid) {
        User currentUser = findUserByUuid(currentUserUuid);

        // 1. 내가 참여한 방 목록을 ChatRoom과 함께 JOIN FETCH로 가져옴
        List<ChatParticipant> participations = chatParticipantRepository.findByUserWithChatRoom(currentUser);

        // 2. DTO로 변환
        List<ChatRoomListDto> dtos = participations.stream()
                .map(p -> convertToDto(p, currentUser))
                .collect(Collectors.toList());

        // 3. 마지막 메시지 시간순으로 정렬 (최신순)
        dtos.sort(Comparator.comparing(ChatRoomListDto::getLastMessageTime, Comparator.nullsLast(Comparator.reverseOrder())));

        return dtos;
    }

    /**
     * 3. 지난 메시지 조회 (API ②)
     * @param pageable (페이지네이션 정보)
     * @return Page<ChatMessageDto>
     */
    @Transactional(readOnly = true)
    public Page<ChatMessageDto> getMessages(String currentUserUuid, Long roomId, Pageable pageable) {
        User currentUser = findUserByUuid(currentUserUuid);
        ChatRoom room = findRoomById(roomId);

        // [보안] 이 방의 참여자가 맞는지 확인
        ChatParticipant participant = chatParticipantRepository.findByUserAndChatRoom(currentUser, room)
                .orElseThrow(() -> new RuntimeException("채팅방에 참여한 사용자가 아닙니다."));

        // 1. 메시지 목록을 페이지네이션으로 조회
        Page<ChatMessage> messages = chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(room, pageable);

        // 2. [안 읽은 수 계산] 방의 총 인원 수
        long totalParticipants = chatParticipantRepository.countByChatRoom(room);

        // 3. DTO로 변환 (N+1 발생 지점: 안 읽은 수 계산)
        // (성능 경고: 메시지 30개를 가져오면, '안 읽은 수'를 알기 위해 30번의 추가 쿼리가 발생합니다.)
        return messages.map(message -> {
            long readCount = messageReadStatusRepository.countByChatMessage(message);
            long unreadCount = totalParticipants - readCount;
            return ChatMessageDto.from(message, unreadCount < 0 ? 0 : unreadCount);
        });
    }

    @Transactional
    public void readChatRoom(String currentUserUuid, Long roomId) {
        User currentUser = findUserByUuid(currentUserUuid); // (findUserByUuid는 서비스 내에 구현되어 있다고 가정)
        ChatRoom room = findRoomById(roomId);           // (findRoomById는 서비스 내에 구현되어 있다고 가정)

        // 1. (권한 확인) 현재 참여자 정보 조회
        ChatParticipant participant = chatParticipantRepository.findByUserAndChatRoom(currentUser, room)
                .orElseThrow(() -> new RuntimeException("채팅방에 참여한 사용자가 아닙니다."));

        // 2. [수정] 이 유저가 이 방에서 '아직 안 읽은' 모든 메시지 조회 (커스텀 쿼리)
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessagesByUserInRoom(room, currentUser);

        if (unreadMessages.isEmpty()) {
            // 안 읽은 메시지가 없으면 책갈피만 업데이트하고 종료
            updateParticipantBookmark(participant, room);
            return;
        }

        // 3. [수정] 안 읽은 메시지들을 '읽음' 상태(MessageReadStatus)로 일괄 생성
        List<MessageReadStatus> newReadStatuses = new ArrayList<>();
        for (ChatMessage message : unreadMessages) {
            MessageReadStatus readStatus = MessageReadStatus.builder()
                    .chatMessage(message)
                    .user(currentUser)
                    .build();
            newReadStatuses.add(readStatus);
        }

        // 4. [수정] DB에 일괄 저장 (Batch Insert)
        messageReadStatusRepository.saveAll(newReadStatuses);

        // 5. [수정] 책갈피(lastReadMessageId) 업데이트 (기존 로직 수행)
        updateParticipantBookmark(participant, room);
    }

    /**
     * 책갈피 업데이트 로직 (중복 제거용)
     */
    private void updateParticipantBookmark(ChatParticipant participant, ChatRoom room) {
        // 방의 '가장 마지막' 메시지 ID 찾기
        Optional<ChatMessage> lastMessage = chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(room);

        if (lastMessage.isPresent()) {
            // '책갈피(lastReadMessageId)'를 마지막 메시지 ID로 업데이트
            participant.setLastReadMessageId(lastMessage.get().getChatMessageId());
            chatParticipantRepository.save(participant);
        }
        // (메시지가 없으면 아무것도 안 함)
    }


    // --- Helper Methods ---

    /**
     * (채팅 목록 DTO 변환 헬퍼)
     */
    private ChatRoomListDto convertToDto(ChatParticipant participant, User currentUser) {
        ChatRoom room = participant.getChatRoom();

        // 1. 마지막 메시지 조회 (N+1 발생 지점)
        ChatMessage lastMessage = chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(room).orElse(null);

        // 2. 안 읽음 여부(isUnread) 계산
        long lastReadId = (participant.getLastReadMessageId() != null) ? participant.getLastReadMessageId() : 0L;
        long lastMessageId = (lastMessage != null) ? lastMessage.getChatMessageId() : 0L;
        boolean isUnread = lastMessageId > lastReadId;

        // 3. 방 이름, 프사 설정
        String roomName = room.getRoomName();
        String profileImage = null; // (그룹일 경우 기본 이미지 등)

        if (room.getType() == ChatRoomType.ONE_TO_ONE) {
            // 1:1방이면 상대방 정보를 이름/프사로 설정
            User otherUser = room.getUser1().getUserUuid().equals(currentUser.getUserUuid()) ? room.getUser2() : room.getUser1();
            roomName = otherUser.getNickname();
            profileImage = otherUser.getProfileImagePath();
        }

        return ChatRoomListDto.builder()
                .roomId(room.getChatRoomId())
                .roomName(roomName)
                .profileImagePath(profileImage)
                .lastMessageContent(lastMessage != null ? lastMessage.getContent() : "아직 대화가 없습니다.")
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : room.getCreatedAt())
                .isUnread(isUnread)
                .build();
    }

    private ChatRoom findRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
    }


    // --- Helper Methods ---

    private User findUserByUuid(String userUuid) {
        return userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new RuntimeException("User not found with uuid: " + userUuid));
    }

    private String generateDefaultGroupName(User creator, List<User> users) {
        // (예시: "유저1, 유저2, 유저3 님과의 대화")
        String names = users.stream().map(User::getNickname).limit(3).reduce((a, b) -> a + ", " + b).orElse("");
        return creator.getNickname() + ", " + names + "...";
    }
}