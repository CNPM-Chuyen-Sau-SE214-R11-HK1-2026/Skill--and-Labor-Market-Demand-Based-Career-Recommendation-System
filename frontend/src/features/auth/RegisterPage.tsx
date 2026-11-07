import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../../hooks/useAuth';
import { Input } from '../../components/common/Input';
import { Button } from '../../components/common/Button';
import { Alert } from '../../components/common/Alert';
import type { ApiErrorResponse } from '../../types/api';
import styles from './RegisterPage.module.css';

export const RegisterPage: React.FC = () => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    setSuccessMessage(null);

    // Client-side validations
    if (!fullName.trim()) {
      setErrorMessage('Vui lòng nhập họ và tên');
      return;
    }

    if (!email.trim()) {
      setErrorMessage('Vui lòng nhập địa chỉ email');
      return;
    }

    if (password.length < 8) {
      setErrorMessage('Mật khẩu phải chứa ít nhất 8 ký tự');
      return;
    }

    if (password !== confirmPassword) {
      setErrorMessage('Mật khẩu xác nhận không khớp');
      return;
    }

    setIsSubmitting(true);

    try {
      await register({
        fullName: fullName.trim(),
        email: email.trim(),
        password,
        phone: phone.trim() || undefined,
      });

      setSuccessMessage('Đăng ký tài khoản thành công! Đang chuyển đến trang đăng nhập...');
      setTimeout(() => {
        navigate('/login');
      }, 1500);
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) {
        const errorData = err.response?.data as ApiErrorResponse | undefined;
        if (err.response?.status === 409) {
          setErrorMessage('Địa chỉ email này đã được đăng ký');
        } else if (errorData?.message) {
          setErrorMessage(errorData.message);
        } else {
          setErrorMessage('Không thể hoàn tất đăng ký. Vui lòng kiểm tra lại thông tin.');
        }
      } else if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('Đã xảy ra lỗi không xác định');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={styles.authContainer}>
      <div className={styles.authCard}>
        <div className={styles.authHeader}>
          <h2 className={styles.authTitle}>Đăng ký ứng viên</h2>
          <p className={styles.authSubtitle}>
            Tạo tài khoản để khám phá nghề nghiệp và nhận gợi ý việc làm phù hợp
          </p>
        </div>

        {errorMessage && <Alert type="error" message={errorMessage} />}
        {successMessage && <Alert type="success" message={successMessage} />}

        <form onSubmit={handleSubmit} noValidate>
          <Input
            label="Họ và tên"
            type="text"
            placeholder="Nguyễn Văn A"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            disabled={isSubmitting}
            required
            autoComplete="name"
          />

          <Input
            label="Email"
            type="email"
            placeholder="candidate@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={isSubmitting}
            required
            autoComplete="email"
          />

          <Input
            label="Số điện thoại (tùy chọn)"
            type="tel"
            placeholder="0901234567"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            disabled={isSubmitting}
            autoComplete="tel"
          />

          <Input
            label="Mật khẩu (tối thiểu 8 ký tự)"
            type="password"
            placeholder="Nhập mật khẩu..."
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={isSubmitting}
            required
            autoComplete="new-password"
          />

          <Input
            label="Xác nhận mật khẩu"
            type="password"
            placeholder="Nhập lại mật khẩu..."
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            disabled={isSubmitting}
            required
            autoComplete="new-password"
          />

          <Button type="submit" variant="primary" isLoading={isSubmitting} className={styles.submitBtn}>
            Tạo tài khoản
          </Button>
        </form>

        <div className={styles.authFooter}>
          <p>
            Đã có tài khoản?{' '}
            <Link to="/login" className={styles.link}>
              Đăng nhập
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
