package com.example.demo.config;

/**
 * Centralized constants for all API endpoints in the system,
 * categorized by access level (PUBLIC/PRIVATE) and functional module.
 */
public final class SecurityEndpoints {

    private SecurityEndpoints() {
        // Prevent instantiation
    }

    public static final class PUBLIC {
        public static final String[] AUTH = {
            "/api/v1/auth/login",
            "/api/v1/auth/admin/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/refresh",
            "/api/v1/auth/register/otp",
            "/api/v1/auth/register/verify"
        };

        public static final String[] USER = {
            "/api/v1/users/password/reset/otp",
            "/api/v1/users/password/reset/verify",
            "/api/v1/users/{username}"
        };

        public static final String[] QUIZ = {
            "/api/v1/quiz/public"
        };

        public static final String[] FORM = {
            "/api/v1/form/getAll",
            "/api/v1/form/topic",
            "/api/v1/form/topics",
            "/api/v1/form/{topicId}",
            "/api/v1/form/topic/{topicId}/tags",
            "/api/v1/form/{formId}/getComment",
            "/api/v1/form/{formId}/getVote",
            "/api/v1/form/search",
            "/api/v1/discussion/**"
        };

        public static final String[] FILE = {
            "/api/webhook/cloudinary",
            "/api/v1/majors",
            "/api/v1/pdfs/**",
            "/api/v1/mail/donate",
            "/api/v1/mail/send-bug"
        };

        public static final String[] INFRA = {
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
        };

        /**
         * Returns all public endpoints for permitAll() and CSRF ignoring.
         */
        public static String[] all() {
            return join(AUTH, USER, QUIZ, FORM, FILE, INFRA);
        }
    }

    public static final class PRIVATE {
        public static final String[] ADMIN = {
            "/api/v1/admin/**",
            "/api/v1/discussion/newtopic",
            "/api/v1/discussion/delete/**",
            "/api/v1/user" // Admin GET
        };

        public static final String[] QUIZ = {
            "/api/v1/quiz/private",
            "/api/v1/quiz/submit",
            "/api/v1/quiz/stats/**",
            "/api/v1/quiz/topics/**",
            "/api/v1/quiz/archive/**",
            "/api/v1/quiz/bank/**"
        };

        public static final String[] USER = {
            "/api/v1/users/me",
            "/api/v1/users/password/change",
            "/api/v1/users/profile/update"
        };

        public static final String[] COMMUNITY = {
            "/api/v1/form/{topicId}/newForm",
            "/api/v1/form/newTopic",
            "/api/v1/form/{formId}/delete",
            "/api/v1/form/session/**",
            "/api/v1/comment/**",
            "/api/v1/vote/**"
        };

        public static final String[] DOCUMENT = {
            "/api/v1/reading-progress/**",
            "/api/v1/annotations/**"
        };
    }

    private static String[] join(String[]... arrays) {
        int length = 0;
        for (String[] array : arrays) {
            length += array.length;
        }
        String[] result = new String[length];
        int pos = 0;
        for (String[] array : arrays) {
            System.arraycopy(array, 0, result, pos, array.length);
            pos += array.length;
        }
        return result;
    }
}
