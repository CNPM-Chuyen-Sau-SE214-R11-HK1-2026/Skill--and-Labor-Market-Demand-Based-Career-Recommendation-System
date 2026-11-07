import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../../hooks/useAuth';
import { Input } from '../../components/common/Input';
import { Button } from '../../components/common/Button';
import { Alert } from '../../components/common/Alert';
import type { ApiErrorResponse } from '../../types/api';
import styles from './LoginPage.module.css';

export const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    // Client-side validation
    if (!email.trim()) {
      setErrorMessage('Vui lòng nhập địa chỉ email');
      return;
    }

    if (!password) {
      setErrorMessage('Vui lòng nhập mật khẩu');
      return;
    }

    setIsSubmitting(true);

    try {
      await login({ email: email.trim(), password });
      navigate('/dashboard', { replace: true });
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) {
        const errorData = err.response?.data as ApiErrorResponse | undefined;
        if (err.response?.status === 401) {
          setErrorMessage(errorData?.message || 'Email hoặc mật khẩu không chính xác');
        } else if (errorData?.message) {
          setErrorMessage(errorData.message);
        } else {
          setErrorMessage('Không thể kết nối đến máy chủ. Vui lòng thử lại sau.');
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
          <h2 className={styles.authTitle}>Đăng nhập tài khoản</h2>
          <p className={styles.authSubtitle}>
            Hệ thống gợi ý nghề nghiệp dựa trên kỹ năng &amp; nhu cầu thị trường
          </p>
        </div>

        {errorMessage && <Alert type="error" message={errorMessage} />}

        <form onSubmit={handleSubmit} noValidate>
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
            label="Mật khẩu"
            type="password"
            placeholder="Nhập mật khẩu..."
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={isSubmitting}
            required
            autoComplete="current-password"
          />

          <Button type="submit" variant="primary" isLoading={isSubmitting} className={styles.submitBtn}>
            Đăng nhập
          </Button>
        </form>

        <div className={styles.authFooter}>
          <p>
            Chưa có tài khoản?{' '}
            <Link to="/register" className={styles.link}>
              Đăng ký ngay
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
