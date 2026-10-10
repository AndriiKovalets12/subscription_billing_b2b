package dev.emails;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import dev.invitation.dto.InvitationDto;
import dev.tenants.TenantService;
import dev.tenants.dto.TenantDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class EmailService {

    private final String corporateEmail;
    private final String frontendUrl;
    private final SendGrid sendGrid;
    private final TenantService tenantService;

    public EmailService(
            @Value("${application.email.corporate-email-address}") String corporateEmail,
            @Value("${application.email.api-key}") String apiKey,
            @Value("${application.frontend.url:https://app.com}") String frontendUrl,
            TenantService tenantService) {

        this.corporateEmail = corporateEmail;
        this.frontendUrl = frontendUrl;
        this.tenantService = tenantService;
        this.sendGrid = new SendGrid(apiKey);
    }

    @Async
    public void sendInvitationEmail(InvitationDto invite, TenantDto tenantDto) {

        String subject = String.format("You've been invited to join %s company.", tenantDto.name());
        String inviteUrl = String.format("%s/invite?token=%s", frontendUrl, invite.token());

        String contentText = String.format("""
                Hello,
                
                %s owner has invited you to join %s as %s.
                
                To accept this invitation and complete your registration, please click the link below:
                
                %s
                
                Note: This invitation link is unique to you. If you were not expecting this invitation, please ignore this email.
                
                Best regards,
                The Support Team
                """, tenantDto.name(), tenantDto.name(), invite.userRole(), inviteUrl);

        Email from = new Email(corporateEmail);
        Email to = new Email(invite.email());
        Content content = new Content("text/plain", contentText);

        Mail mail = new Mail(from, subject, to, content);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Invitation email successfully sent to {}. Status code: {}", invite.email(), response.getStatusCode());
            } else {
                log.error("Failed to send email to {}. SendGrid returned status: {}, body: {}",
                        invite.email(), response.getStatusCode(), response.getBody());
                throw new IllegalStateException("Failed to send invitation email via SendGrid. Status: " + response.getStatusCode());
            }

        } catch (IOException e) {
            log.error("IOException occurred while sending email to {}: {}", invite.email(), e.getMessage());
            throw new RuntimeException("Email sending failed due to network or I/O issue", e);
        }
    }
}