package maksim.userservice.utils.validators;

import maksim.userservice.models.dtos.CreateUserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateUserDtoValidators {
    private final StringValidators stringValidators;

    @Autowired
    public CreateUserDtoValidators(StringValidators stringValidators) {
        this.stringValidators = stringValidators;
    }

    public boolean isValid(CreateUserDto dto) {
        if (isEmpty(dto) || isPasswordValid(dto.getPassword())) {
            return false;
        }

        screenStringValues(dto);

        return isSafeFromSqlInjection(dto);
    }

    public boolean isEmpty(CreateUserDto dto) {
        return dto.getPassword() == null || dto.getEmail() == null || dto.getName() == null;
    }

    public boolean isPasswordValid(String password) {
        boolean isPasswordContainsDigit = false;
        boolean isPasswordContainsSpecs = false;
        boolean isPasswordContainsLetters = false;

        byte[] chars = password.getBytes();

        for (int i = 0; i < password.length(); i++) {
            if (!isPasswordContainsDigit && (chars[i] >= 48 && chars[i] <= 57)) {
                isPasswordContainsDigit = true;
                continue;
            }

            if (!isPasswordContainsLetters && ((chars[i] >= 65 && chars[i] <= 90) || (chars[i] >= 97 && chars[i] <= 122))) {
                isPasswordContainsLetters = true;
                continue;
            }

            if (!isPasswordContainsSpecs && (chars[i] >= 33 && chars[i] <= 42)) {
                isPasswordContainsSpecs = true;
            }
        }

        return isPasswordContainsDigit && isPasswordContainsLetters && isPasswordContainsSpecs;
    }

    public void screenStringValues(CreateUserDto dto) {
        dto.setPassword(stringValidators.textScreening(dto.getPassword()));
        dto.setEmail(stringValidators.textScreening(dto.getEmail()));
        dto.setName(stringValidators.textScreening(dto.getName()));
    }

    public boolean isSafeFromSqlInjection(CreateUserDto dto) {
        return stringValidators.isSafeFromSqlInjection(dto.getEmail())
            && stringValidators.isSafeFromSqlInjection(dto.getPassword())
            && stringValidators.isSafeFromSqlInjection(dto.getName());
    }

}
