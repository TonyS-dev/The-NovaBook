package com.codeup.novabook.service;

import com.codeup.novabook.domain.Member;
import com.codeup.novabook.domain.MemberStatus;
import com.codeup.novabook.exception.MemberAlreadyExistsException;
import com.codeup.novabook.exception.MemberNotFoundException;
import com.codeup.novabook.exception.ValidationException;
import com.codeup.novabook.repo.IMemberRepository;
import com.codeup.novabook.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MemberService implementation.
 * 
 * <p>Tests cover:
 * <ul>
 *   <li>Member registration with document ID and email validation</li>
 *   <li>Profile updates</li>
 *   <li>Status management (activate, deactivate, suspend)</li>
 *   <li>Loan eligibility checks</li>
 *   <li>Document ID and email uniqueness</li>
 * </ul>
 * 
 * @author TonyS-dev/Antonio Santiago
 */
@DisplayName("MemberService Tests")
class MemberServiceTest {

    @Mock
    private IMemberRepository memberRepository;

    private IMemberService memberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        memberService = new MemberServiceImpl(memberRepository);
    }

    // ==================== MEMBER REGISTRATION TESTS ====================

    @Test
    @DisplayName("Should register member with valid inputs")
    void testRegisterMemberWithValidInputs() throws Exception {
        // Arrange
        String firstName = "Juan";
        String lastName = "Pérez";
        String documentId = "12345678";
        String email = "juan.perez@email.com";
        String phone = "1234567890";
        String address = "Calle Principal 123";
        
        Member createdMember = new Member();
        createdMember.setId(1);
        createdMember.setFirstName(firstName);
        createdMember.setLastName(lastName);
        createdMember.setDocumentId(documentId);
        createdMember.setEmail(email);
        createdMember.setPhone(phone);
        createdMember.setAddress(address);
        createdMember.setStatus(MemberStatus.ACTIVE);
        
        when(memberRepository.existsByDocumentId(documentId)).thenReturn(false);
        when(memberRepository.existsByEmail(email)).thenReturn(false);
        when(memberRepository.create(any(Member.class))).thenReturn(createdMember);

        // Act
        Member result = memberService.registerMember(firstName, lastName, documentId, email, phone, address);

        // Assert
        assertNotNull(result);
        assertEquals(firstName, result.getFirstName());
        assertEquals(documentId, result.getDocumentId());
        verify(memberRepository, times(1)).existsByDocumentId(documentId);
        verify(memberRepository, times(1)).existsByEmail(email);
        verify(memberRepository, times(1)).create(argThat(member ->
            member.getFirstName().equals(firstName) &&
            member.getLastName().equals(lastName) &&
            member.getDocumentId().equals(documentId) &&
            member.getEmail().equals(email) &&
            member.getStatus() == MemberStatus.ACTIVE
        ));
    }

    @Test
    @DisplayName("Should throw MemberAlreadyExistsException when document ID exists")
    void testRegisterMemberWithDuplicateDocumentId() {
        // Arrange
        String documentId = "12345678";
        when(memberRepository.existsByDocumentId(documentId)).thenReturn(true);

        // Act & Assert
        assertThrows(MemberAlreadyExistsException.class, () ->
            memberService.registerMember("Juan", "Pérez", documentId, "juan@email.com", "1234567890", "Address")
        );
        verify(memberRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should throw MemberAlreadyExistsException when email exists")
    void testRegisterMemberWithDuplicateEmail() {
        // Arrange
        String email = "juan@email.com";
        when(memberRepository.existsByDocumentId(anyString())).thenReturn(false);
        when(memberRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        assertThrows(MemberAlreadyExistsException.class, () ->
            memberService.registerMember("Juan", "Pérez", "12345678", email, "1234567890", "Address")
        );
        verify(memberRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should register member even with empty first name (validation checks fullName)")
    void testRegisterMemberWithEmptyFirstName() {
        // Arrange - ValidationUtils validates fullName (firstName + " " + lastName)
        // So " Doe" passes validation (not empty)
        Member createdMember = new Member();
        createdMember.setId(1);
        createdMember.setFirstName("");
        createdMember.setLastName("Doe");
        createdMember.setDocumentId("12345678");
        createdMember.setEmail("john.doe@example.com");
        createdMember.setPhone("123456789");
        createdMember.setStatus(MemberStatus.ACTIVE);
        
        when(memberRepository.existsByDocumentId("12345678")).thenReturn(false);
        when(memberRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(memberRepository.create(any(Member.class))).thenReturn(createdMember);
        
        // Act
        Member result = memberService.registerMember("", "Doe", "12345678", 
                                                    "john.doe@example.com", "123456789", "123 Main St");
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("", result.getFirstName());
        
        // Verify repository was called
        verify(memberRepository, times(1)).create(any(Member.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when document ID is empty")
    void testRegisterMemberWithEmptyDocumentId() {
        // Arrange
        String documentId = "";

        // Act & Assert
        assertThrows(ValidationException.class, () ->
            memberService.registerMember("Juan", "Pérez", documentId, "juan@email.com", "1234567890", "Address")
        );
        verify(memberRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when email is invalid")
    void testRegisterMemberWithInvalidEmail() {
        // Arrange
        String email = "invalid-email";

        // Act & Assert
        assertThrows(ValidationException.class, () ->
            memberService.registerMember("Juan", "Pérez", "12345678", email, "1234567890", "Address")
        );
        verify(memberRepository, never()).create(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when phone is invalid")
    void testRegisterMemberWithInvalidPhone() {
        // Arrange
        String phone = "ABC123";

        // Act & Assert
        assertThrows(ValidationException.class, () ->
            memberService.registerMember("Juan", "Pérez", "12345678", "juan@email.com", phone, "Address")
        );
        verify(memberRepository, never()).create(any());
    }

    // ==================== UPDATE PROFILE TESTS ====================

    @Test
    @DisplayName("Should update member profile successfully")
    void testUpdateMemberProfile() throws Exception {
        // Arrange
        Integer memberId = 1;
        String firstName = "Juan Updated";
        String lastName = "Pérez Updated";
        String phone = "9876543210";
        String address = "New Address 456";
        
        Member existingMember = new Member();
        existingMember.setId(memberId);
        existingMember.setFirstName("Juan");
        existingMember.setLastName("Pérez");
        existingMember.setPhone("1234567890");
        existingMember.setAddress("Old Address");
        existingMember.setStatus(MemberStatus.ACTIVE);
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(existingMember));

        // Act
        memberService.updateMemberProfile(memberId, firstName, lastName, phone, address);

        // Assert
        verify(memberRepository, times(1)).findById(memberId);
        verify(memberRepository, times(1)).update(argThat(member ->
            member.getFirstName().equals(firstName) &&
            member.getLastName().equals(lastName) &&
            member.getPhone().equals(phone) &&
            member.getAddress().equals(address)
        ));
    }

    @Test
    @DisplayName("Should throw MemberNotFoundException when updating non-existent member")
    void testUpdateNonExistentMemberProfile() {
        // Arrange
        Integer memberId = 999;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MemberNotFoundException.class, () ->
            memberService.updateMemberProfile(memberId, "Name", "Last", "1234567890", "Address")
        );
        verify(memberRepository, never()).update(any());
    }

    // ==================== STATUS MANAGEMENT TESTS ====================

    @Test
    @DisplayName("Should activate member successfully")
    void testActivateMember() throws Exception {
        // Arrange
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(MemberStatus.INACTIVE);
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // Act
        memberService.activateMember(memberId);

        // Assert
        verify(memberRepository, times(1)).update(argThat(m -> m.getStatus() == MemberStatus.ACTIVE));
    }

    @Test
    @DisplayName("Should deactivate member successfully")
    void testDeactivateMember() throws Exception {
        // Arrange
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(MemberStatus.ACTIVE);
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // Act
        memberService.deactivateMember(memberId);

        // Assert
        verify(memberRepository, times(1)).update(argThat(m -> m.getStatus() == MemberStatus.INACTIVE));
    }

    @Test
    @DisplayName("Should suspend member successfully")
    void testSuspendMember() throws Exception {
        // Arrange
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(MemberStatus.ACTIVE);
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // Act
        memberService.suspendMember(memberId);

        // Assert
        verify(memberRepository, times(1)).update(argThat(m -> m.getStatus() == MemberStatus.SUSPENDED));
    }

    // ==================== LOAN ELIGIBILITY TESTS ====================

    @Test
    @DisplayName("Should return true when member is eligible for loan")
    void testIsEligibleForLoanReturnsTrue() throws Exception {
        // Arrange
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(MemberStatus.ACTIVE);
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // Act
        boolean result = memberService.isEligibleForLoan(memberId);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when member is inactive")
    void testIsEligibleForLoanReturnsFalseWhenInactive() throws Exception {
        // Arrange
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(MemberStatus.INACTIVE);
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // Act
        boolean result = memberService.isEligibleForLoan(memberId);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when member is suspended")
    void testIsEligibleForLoanReturnsFalseWhenSuspended() throws Exception {
        // Arrange
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(MemberStatus.SUSPENDED);
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // Act
        boolean result = memberService.isEligibleForLoan(memberId);

        // Assert
        assertFalse(result);
    }

    // ==================== EXISTENCE CHECKS TESTS ====================

    @Test
    @DisplayName("Should return true when document ID exists")
    void testDocumentIdExistsReturnsTrue() {
        // Arrange
        String documentId = "12345678";
        when(memberRepository.existsByDocumentId(documentId)).thenReturn(true);

        // Act
        boolean result = memberService.documentIdExists(documentId);

        // Assert
        assertTrue(result);
        verify(memberRepository, times(1)).existsByDocumentId(documentId);
    }

    @Test
    @DisplayName("Should return false when document ID does not exist")
    void testDocumentIdExistsReturnsFalse() {
        // Arrange
        String documentId = "99999999";
        when(memberRepository.existsByDocumentId(documentId)).thenReturn(false);

        // Act
        boolean result = memberService.documentIdExists(documentId);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return true when email exists")
    void testEmailExistsReturnsTrue() {
        // Arrange
        String email = "juan@email.com";
        when(memberRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean result = memberService.emailExists(email);

        // Assert
        assertTrue(result);
        verify(memberRepository, times(1)).existsByEmail(email);
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void testEmailExistsReturnsFalse() {
        // Arrange
        String email = "nonexistent@email.com";
        when(memberRepository.existsByEmail(email)).thenReturn(false);

        // Act
        boolean result = memberService.emailExists(email);

        // Assert
        assertFalse(result);
    }

    // ==================== GET MEMBERS TESTS ====================

    @Test
    @DisplayName("Should return active members")
    void testGetActiveMembers() {
        // Arrange
        Member member1 = new Member();
        member1.setId(1);
        member1.setFirstName("Juan");
        member1.setStatus(MemberStatus.ACTIVE);
        
        Member member2 = new Member();
        member2.setId(2);
        member2.setFirstName("María");
        member2.setStatus(MemberStatus.ACTIVE);
        
        List<Member> activeMembers = List.of(member1, member2);
        when(memberRepository.findByStatus(MemberStatus.ACTIVE)).thenReturn(activeMembers);

        // Act
        List<Member> result = memberService.getActiveMembers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(m -> m.getStatus() == MemberStatus.ACTIVE));
    }

    @Test
    @DisplayName("Should return member by ID")
    void testGetMemberById() throws Exception {
        // Arrange
        Integer memberId = 1;
        Member member = new Member();
        member.setId(memberId);
        member.setFirstName("Juan");
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // Act
        Member result = memberService.getMemberById(memberId);

        // Assert
        assertNotNull(result);
        assertEquals(memberId, result.getId());
        assertEquals("Juan", result.getFirstName());
    }

    @Test
    @DisplayName("Should throw MemberNotFoundException when member not found by ID")
    void testGetMemberByIdThrowsException() {
        // Arrange
        Integer memberId = 999;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MemberNotFoundException.class, () ->
            memberService.getMemberById(memberId)
        );
    }
}
