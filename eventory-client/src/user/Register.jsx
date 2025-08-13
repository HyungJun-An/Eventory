import React from 'react';
import '../assets/css/Register.css';

const Register = () => {
  // Input Component
  const Input = ({
    state,
    size,
    className,
    frameClassName,
    icon,
    textClassName,
    text,
    visible = true,
    rightIcon,
    visible1 = true,
  }) => {
    return (
      <div className={`input ${state} ${size} ${className}`}>
        <div className={`frame-2 ${frameClassName}`}>
          {visible1 && icon}
          <div className={`text ${textClassName}`}>{text}</div>
        </div>
        {visible && rightIcon}
      </div>
    );
  };

  // SearchBoxDate Component
  const SearchBoxDate = ({
    state,
    className,
    timeCalendarThirtyTimeCalendarThirtyClassName,
    divClassName,
    arrowsDownArrowsDownClassName,
    icon,
    text = "Date",
  }) => {
    return (
      <div className={`search-box-date ${className}`}>
        {icon}
        <div className={`date ${divClassName}`}>{text}</div>
        <img
          className={arrowsDownArrowsDownClassName}
          alt="Arrows down"
          src="https://c.animaapp.com/me9i7i1uM2B4jc/img/arrows-down.svg"
        />
      </div>
    );
  };

  // Buttons Component
  const Buttons = ({
    style,
    state,
    color,
    size,
    className,
    leftIcon,
    text = "Text",
    visible = true,
  }) => {
    return (
      <div className={`buttons ${className}`}>
        {leftIcon}
        <div className="text-2">{text}</div>
        {visible && (
          <img
            className="design-component-instance-node-4"
            alt="Arrows right small"
            src="https://c.animaapp.com/me9i7i1uM2B4jc/img/arrows-right-small.svg"
          />
        )}
      </div>
    );
  };

  return (
    <div className="register" data-model-id="11507:13782">
      {/* Main Form Frame */}
      <div className="frame">
        <div className="frame-wrapper">
          <div className="div">
            <div className="div-2">
              <div className="div-3">
                {/* Sign in with Google Button */}
                <Input
                  className="input-instance"
                  frameClassName="design-component-instance-node"
                  icon={
                    <img
                      className="design-component-instance-node-2"
                      alt="Gmail"
                      src="https://c.animaapp.com/me9i7i1uM2B4jc/img/base-mail-1.svg"
                    />
                  }
                  size="fifty-six"
                  state="normal"
                  text="Sign in with Google"
                  textClassName="input-2"
                  visible={false}
                />

                {/* OR Divider */}
                <div className="div-4">
                  <div className="rectangle" />
                  <div className="text-wrapper">or</div>
                  <div className="rectangle" />
                </div>

                {/* Form Fields */}
                <div className="div-5">
                  {/* ID Field */}
                  <div className="div-6">
                    <div className="text-wrapper-2">Id</div>
                    <Input
                      className="input-3"
                      icon={
                        <img
                          className="design-component-instance-node-2"
                          alt="Peoples user"
                          src="https://c.animaapp.com/me2azzmxxO3KsY/img/peoples-user.svg"
                        />
                      }
                      size="forty-eight"
                      state="norma"
                      text="Enter your Id"
                      visible={false}
                    />
                  </div>

                  {/* Name Field */}
                  <div className="div-7">
                    <div className="text-wrapper-2">Name</div>
                    <Input
                      className="input-3"
                      icon={
                        <img
                          className="design-component-instance-node-2"
                          alt="Peoples edit name"
                          src="https://c.animaapp.com/me9i7i1uM2B4jc/img/peoples-edit-name.svg"
                        />
                      }
                      size="forty-eight"
                      state="norma"
                      text="Enter your first name"
                      visible={false}
                    />
                  </div>

                  {/* Email Field */}
                  <div className="div-7">
                    <div className="text-wrapper-2">Email</div>
                    <Input
                      className="input-3"
                      icon={
                        <img
                          className="design-component-instance-node-2"
                          alt="Base mail"
                          src="https://c.animaapp.com/me9i7i1uM2B4jc/img/base-mail-2.svg"
                        />
                      }
                      size="forty-eight"
                      state="norma"
                      text="Enter your email"
                      visible={false}
                    />
                  </div>

                  {/* Password Field */}
                  <div className="div-7">
                    <div className="div-7">
                      <div className="text-wrapper-2">Password</div>
                      <Input
                        className="input-3"
                        icon={
                          <img
                            className="design-component-instance-node-2"
                            alt="Base lock"
                            src="https://c.animaapp.com/me9i7i1uM2B4jc/img/base-lock.svg"
                          />
                        }
                        rightIcon={
                          <img
                            className="design-component-instance-node-3"
                            alt="Base preview close"
                            src="https://c.animaapp.com/me9i7i1uM2B4jc/img/base-preview-close-one.svg"
                          />
                        }
                        size="forty-eight"
                        state="norma"
                        text="Create a password"
                      />
                    </div>
                    <p className="p">Must be at least 8 characters.</p>
                  </div>

                  {/* Confirm Password Field */}
                  <div className="div-7">
                    <div className="text-wrapper-2">Confirm Password</div>
                    <Input
                      className="input-3"
                      icon={
                        <img
                          className="design-component-instance-node-2"
                          alt="Base lock"
                          src="https://c.animaapp.com/me9i7i1uM2B4jc/img/base-lock.svg"
                        />
                      }
                      rightIcon={
                        <img
                          className="design-component-instance-node-3"
                          alt="Base preview close"
                          src="https://c.animaapp.com/me9i7i1uM2B4jc/img/base-preview-close-one.svg"
                        />
                      }
                      size="forty-eight"
                      state="norma"
                      text="Confirm your password"
                    />
                  </div>

                  {/* Birth Date and Gender Fields */}
                  <div className="div-7">
                    <div className="div-8">
                      <div className="text-wrapper-3">Birth Date</div>
                      <div className="text-wrapper-4">Gender</div>
                    </div>
                    <div className="div-9">
                      <SearchBoxDate
                        arrowsDownArrowsDownClassName="design-component-instance-node-3"
                        className="search-box-date-picker"
                        divClassName="search-box-date-instance"
                        state="normal"
                        icon={
                          <img
                            className="design-component-instance-node-3"
                            alt="Time calendar thirty"
                            src="https://c.animaapp.com/me9i7i1uM2B4jc/img/time-calendar-thirty-two-1.svg"
                          />
                        }
                      />
                      <SearchBoxDate
                        arrowsDownArrowsDownClassName="design-component-instance-node-3"
                        className="search-box-date-picker-instance"
                        divClassName="search-box-date-instance"
                        icon={
                          <img
                            className="design-component-instance-node-3"
                            alt="Peoples user"
                            src="https://c.animaapp.com/me2azzmxxO3KsY/img/peoples-user.svg"
                          />
                        }
                        state="normal"
                        text="Select"
                      />
                    </div>
                  </div>

                  {/* Phone Number Field */}
                  <div className="div-7">
                    <div className="div-7">
                      <div className="text-wrapper-2">Phone Number</div>
                      <Input
                        className="input-3"
                        icon={
                          <img
                            className="design-component-instance-node-2"
                            alt="Base phone telephone"
                            src="https://c.animaapp.com/me9i7i1uM2B4jc/img/base-mail.svg"
                          />
                        }
                        size="forty-eight"
                        state="norma"
                        text="Enter your phone number"
                        visible={false}
                      />
                    </div>
                  </div>
                </div>
              </div>

              {/* Next Button */}
              <Input
                className="input-4"
                size="fifty-six"
                state="colored"
                text="next"
                textClassName="input-5"
                visible={false}
                visible1={false}
              />
            </div>

            {/* Login Link */}
            <div className="div-10">
              <div className="text-wrapper-5">Already have an account?</div>
              <div className="text-wrapper-6">Log In</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
