package com.codeup.novabook.service.impl;

import com.codeup.novabook.domain.Member;
import com.codeup.novabook.domain.MemberStatus;
import com.codeup.novabook.exception.*;
import com.codeup.novabook.repo.IMemberRepository;
import com.codeup.novabook.service.IMemberService;
import com.codeup.novabook.util.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service implementation for Member management with business validations.
 */
public class MemberServiceImpl implements IMemberService {
    
    private static final Logger LOGGER = Logger.getLogger(MemberServiceImpl.class.getName());
    private final IMemberRepository memberRepository;
    
    public MemberServiceImpl(IMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    @Override
    public Member registerMember(String firstName, String lastName, String documentId, 
                                String email, String phone, String address) 
            throws MemberAlreadyExistsException, ValidationException {
        LOGGER.log(Level.INFO, "[POST /api/members/register] Registering new member: {0}", email);
        
        try {
            // Validate using centralized validation
            String fullName = firstName + " " + lastName;
            ValidationUtils.validateMember(documentId, fullName, email, phone);
            
            // Check for existing document ID
            if (memberRepository.existsByDocumentId(documentId)) {
                LOGGER.log(Level.WARNING, "[POST /api/members/register] Document ID already exists: {0}", documentId);
                throw new MemberAlreadyExistsException();
            }
            
            // Check for existing email
            if (memberRepository.existsByEmail(email)) {
                LOGGER.log(Level.WARNING, "[POST /api/members/register] Email already exists: {0}", email);
                throw new MemberAlreadyExistsException();
            }
            
            // Create member with decorator pattern (default values)
            Member member = new Member();
            member.setFirstName(firstName);
            member.setLastName(lastName);
            member.setDocumentId(documentId);
            member.setEmail(email);
            member.setPhone(phone);
            member.setAddress(address);
            member.setStatus(MemberStatus.ACTIVE);
            member.setRegistrationDate(LocalDate.now());
            member.setCreatedAt(LocalDateTime.now());
            
            LOGGER.log(Level.INFO, "[POST /api/members/register] Applied default status: ACTIVE");
            
            Member created = memberRepository.create(member);
            LOGGER.log(Level.INFO, "[POST /api/members/register] Member registered successfully with ID: {0}", created.getId());
            return created;
            
        } catch (MemberAlreadyExistsException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[POST /api/members/register] Registration error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public Member getMemberById(Integer memberId) throws MemberNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/members/{0}] Fetching member by ID", memberId);
        
        try {
            Optional<Member> member = memberRepository.findById(memberId);
            if (member.isEmpty()) {
                LOGGER.log(Level.WARNING, "[GET /api/members/{0}] Member not found", memberId);
                throw new MemberNotFoundException();
            }
            return member.get();
        } catch (MemberNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public Member getMemberByDocumentId(String documentId) throws MemberNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/members/document/{0}] Fetching member by document ID", documentId);
        
        try {
            Optional<Member> member = memberRepository.findByDocumentId(documentId);
            if (member.isEmpty()) {
                LOGGER.log(Level.WARNING, "[GET /api/members/document/{0}] Member not found", documentId);
                throw new MemberNotFoundException();
            }
            return member.get();
        } catch (MemberNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members/document] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public Member getMemberByEmail(String email) throws MemberNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/members/email/{0}] Fetching member by email", email);
        
        try {
            Optional<Member> member = memberRepository.findByEmail(email);
            if (member.isEmpty()) {
                LOGGER.log(Level.WARNING, "[GET /api/members/email/{0}] Member not found", email);
                throw new MemberNotFoundException();
            }
            return member.get();
        } catch (MemberNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members/email] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Member> getAllMembers() {
        LOGGER.log(Level.INFO, "[GET /api/members] Fetching all members");
        
        try {
            List<Member> members = memberRepository.findAll();
            LOGGER.log(Level.INFO, "[GET /api/members] Found {0} members", members.size());
            return members;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Member> getActiveMembers() {
        LOGGER.log(Level.INFO, "[GET /api/members/active] Fetching active members");
        
        try {
            List<Member> members = memberRepository.findByStatus(MemberStatus.ACTIVE);
            LOGGER.log(Level.INFO, "[GET /api/members/active] Found {0} active members", members.size());
            return members;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members/active] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Member> getMembersByStatus(MemberStatus status) {
        LOGGER.log(Level.INFO, "[GET /api/members/status/{0}] Fetching members by status", status);
        
        try {
            List<Member> members = memberRepository.findByStatus(status);
            LOGGER.log(Level.INFO, "[GET /api/members/status/{0}] Found {1} members", 
                      new Object[]{status, members.size()});
            return members;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members/status] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public List<Member> searchMembersByName(String searchTerm) {
        LOGGER.log(Level.INFO, "[GET /api/members/search?name={0}] Searching members by name", searchTerm);
        
        try {
            List<Member> members = memberRepository.searchByName(searchTerm);
            LOGGER.log(Level.INFO, "[GET /api/members/search] Found {0} matching members", members.size());
            return members;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members/search] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public Member updateMemberProfile(Integer memberId, String firstName, String lastName, 
                                     String phone, String address) 
            throws MemberNotFoundException, ValidationException {
        LOGGER.log(Level.INFO, "[PATCH /api/members/{0}/profile] Updating member profile", memberId);
        
        try {
            // Validate name and phone
            String fullName = firstName + " " + lastName;
            ValidationUtils.validateName(fullName);
            if (!ValidationUtils.isEmptyOrNull(phone)) {
                ValidationUtils.validatePhone(phone);
            }
            
            Member member = getMemberById(memberId);
            member.setFirstName(firstName);
            member.setLastName(lastName);
            member.setPhone(phone);
            member.setAddress(address);
            member.setUpdatedAt(LocalDateTime.now());
            
            memberRepository.update(member);
            LOGGER.log(Level.INFO, "[PATCH /api/members/{0}/profile] Profile updated successfully", memberId);
            return member;
            
        } catch (MemberNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[PATCH /api/members/profile] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public Member updateMemberEmail(Integer memberId, String newEmail) 
            throws MemberNotFoundException, MemberAlreadyExistsException, ValidationException {
        LOGGER.log(Level.INFO, "[PATCH /api/members/{0}/email] Updating member email", memberId);
        
        try {
            ValidationUtils.validateEmail(newEmail);
            
            // Check if new email already exists (excluding current member)
            Optional<Member> existingMember = memberRepository.findByEmail(newEmail);
            if (existingMember.isPresent() && !existingMember.get().getId().equals(memberId)) {
                LOGGER.log(Level.WARNING, "[PATCH /api/members/email] Email already exists: {0}", newEmail);
                throw new MemberAlreadyExistsException();
            }
            
            Member member = getMemberById(memberId);
            member.setEmail(newEmail);
            member.setUpdatedAt(LocalDateTime.now());
            
            memberRepository.update(member);
            LOGGER.log(Level.INFO, "[PATCH /api/members/{0}/email] Email updated successfully", memberId);
            return member;
            
        } catch (MemberNotFoundException | MemberAlreadyExistsException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[PATCH /api/members/email] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public Member activateMember(Integer memberId) throws MemberNotFoundException {
        LOGGER.log(Level.INFO, "[PATCH /api/members/{0}/activate] Activating member", memberId);
        
        try {
            Member member = getMemberById(memberId);
            member.setStatus(MemberStatus.ACTIVE);
            member.setUpdatedAt(LocalDateTime.now());
            
            memberRepository.update(member);
            LOGGER.log(Level.INFO, "[PATCH /api/members/{0}/activate] Member activated successfully", memberId);
            return member;
            
        } catch (MemberNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[PATCH /api/members/activate] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public Member deactivateMember(Integer memberId) throws MemberNotFoundException {
        LOGGER.log(Level.INFO, "[PATCH /api/members/{0}/deactivate] Deactivating member", memberId);
        
        try {
            Member member = getMemberById(memberId);
            member.setStatus(MemberStatus.INACTIVE);
            member.setUpdatedAt(LocalDateTime.now());
            
            memberRepository.update(member);
            LOGGER.log(Level.INFO, "[PATCH /api/members/{0}/deactivate] Member deactivated successfully", memberId);
            return member;
            
        } catch (MemberNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[PATCH /api/members/deactivate] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public Member suspendMember(Integer memberId) throws MemberNotFoundException {
        LOGGER.log(Level.INFO, "[PATCH /api/members/{0}/suspend] Suspending member", memberId);
        
        try {
            Member member = getMemberById(memberId);
            member.setStatus(MemberStatus.SUSPENDED);
            member.setUpdatedAt(LocalDateTime.now());
            
            memberRepository.update(member);
            LOGGER.log(Level.INFO, "[PATCH /api/members/{0}/suspend] Member suspended successfully", memberId);
            return member;
            
        } catch (MemberNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[PATCH /api/members/suspend] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public void deleteMember(Integer memberId) throws MemberNotFoundException {
        LOGGER.log(Level.INFO, "[DELETE /api/members/{0}] Soft deleting member", memberId);
        
        try {
            Member member = getMemberById(memberId);
            memberRepository.delete(member.getId());
            LOGGER.log(Level.INFO, "[DELETE /api/members/{0}] Member deleted successfully", memberId);
            
        } catch (MemberNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[DELETE /api/members] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public boolean isEligibleForLoan(Integer memberId) throws MemberNotFoundException {
        LOGGER.log(Level.INFO, "[GET /api/members/{0}/eligible] Checking loan eligibility", memberId);
        
        try {
            Member member = getMemberById(memberId);
            boolean eligible = member.getStatus() == MemberStatus.ACTIVE;
            LOGGER.log(Level.INFO, "[GET /api/members/{0}/eligible] Eligibility: {1}", 
                      new Object[]{memberId, eligible});
            return eligible;
            
        } catch (MemberNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members/eligible] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public boolean documentIdExists(String documentId) {
        LOGGER.log(Level.INFO, "[GET /api/members/exists/document/{0}] Checking document ID existence", documentId);
        
        try {
            return memberRepository.existsByDocumentId(documentId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members/exists/document] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
    
    @Override
    public boolean emailExists(String email) {
        LOGGER.log(Level.INFO, "[GET /api/members/exists/email/{0}] Checking email existence", email);
        
        try {
            return memberRepository.existsByEmail(email);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[GET /api/members/exists/email] Error", e);
            throw new DatabaseException(ErrorCode.DATABASE_ERROR);
        }
    }
}
