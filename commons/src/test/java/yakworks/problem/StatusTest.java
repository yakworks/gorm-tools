package yakworks.problem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import yakworks.api.ApiStatus;
import yakworks.api.HttpStatus;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hobsoft.hamcrest.compose.ComposeMatchers.hasFeature;
import static org.junit.jupiter.api.Assertions.assertThrows;


final class StatusTest {

    @Test
    void shouldHaveReasonPhrase() {
        Stream.of(HttpStatus.values()).forEach(status ->
                assertThat(status, hasFeature("reason phrase", ApiStatus::getReason, is(not(emptyOrNullString())))));
    }

    @Test
    void shouldHaveMeaningfulToString() {
        assertThat(HttpStatus.NOT_FOUND.toString(), equalTo("404: Not Found"));
    }

    @ParameterizedTest
    @CsvSource({
            "409, Conflict",
            "404, Not Found",
            "200, Ok",
            "500, Internal Server Error"
    })
    void shouldHaveCorrectValueFromCode(final int statusCode, final String reasonPhrase) {
        final HttpStatus status = HttpStatus.valueOf(statusCode);

        assertThat(status.getCode(), equalTo(statusCode));
        assertThat(status.getReason(), equalTo(reasonPhrase));
    }

    @Test
    void shouldThrowOnNonExistingCode() {
        assertThrows(IllegalArgumentException.class, () -> HttpStatus.valueOf(111));
    }

}
